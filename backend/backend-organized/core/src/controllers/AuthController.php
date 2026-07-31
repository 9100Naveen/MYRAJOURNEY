<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Utils\Response;
use Src\Utils\Jwt;
use Src\Models\UserModel;
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

class AuthController
{
    private UserModel $users;

    public function __construct()
    {
        $this->users = new UserModel();
    }

    // REGISTER NEW USER
    public function register(): void
    {
        $body = json_decode(file_get_contents('php://input'), true) ?? [];

        $name = trim($body['name'] ?? '');
        $email = trim(strtolower($body['email'] ?? ''));
        $phone = trim($body['phone'] ?? '');
        $password = (string)($body['password'] ?? '');
        $role = strtoupper(trim($body['role'] ?? 'PATIENT'));

        // Basic Validation
        if (!$email || !$password || !$name) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION_ERROR',
                    'message' => 'Name, email and password are required'
                ]
            ], 422);
            return;
        }

        // Check if email already exists
        if ($this->users->findByEmail($email)) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'EMAIL_EXISTS',
                    'message' => 'This email is already registered.'
                ]
            ], 409);
            return;
        }

        try {
            // Create the user
            $uid = $this->users->create([
                'name' => $name,
                'email' => $email,
                'phone' => $phone,
                'password_hash' => password_hash($password, PASSWORD_BCRYPT),
                'role' => $role
            ]);

            if ($uid > 0) {
                $user = $this->users->findById($uid);
                $token = $this->issueToken($uid, $role);
                
                Response::json([
                    'success' => true,
                    'message' => 'Registration successful',
                    'data' => [
                        'token' => $token,
                        'user' => $user
                    ]
                ]);
            } else {
                throw new \Exception("Failed to insert user record");
            }
        } catch (\Exception $e) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'REGISTRATION_FAILED',
                    'message' => 'Registration failed: ' . $e->getMessage()
                ]
            ], 500);
        }
    }

    // LOGIN
    public function login(): void
    {
        $body = json_decode(file_get_contents('php://input'), true) ?? [];

        $email = trim(strtolower($body['email'] ?? ''));
        $password = (string)($body['password'] ?? '');

        $user = $this->users->findByEmail($email);

        // CASE 1: Email does NOT exist → must return 404
        if (!$user) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'EMAIL_NOT_FOUND',
                    'message' => 'Email not found'
                ]
            ], 404);
            return;
        }

        // CASE 2: Email exists but password wrong → 401
        if (!password_verify($password, $user['password_hash'])) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_PASSWORD',
                    'message' => 'Incorrect password'
                ]
            ], 401);
            return;
        }

        // CASE 3: Account not active
        if ($user['status'] !== 'ACTIVE') {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'ACCOUNT_SUSPENDED',
                    'message' => 'Your account has been suspended. Contact administrator.'
                ]
            ], 403);
            return;
        }

        // LOGIN SUCCESS
        $this->users->setLastLogin((int)$user['id']);
        $token = $this->issueToken((int)$user['id'], $user['role']);

        unset($user['password_hash']);

        Response::json([
            'success' => true,
            'data' => [
                'token' => $token,
                'user' => $user
            ]
        ]);
    }

    // GET CURRENT USER
    public function me(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $user = $this->users->findById((int)($auth['uid'] ?? 0));

        Response::json([
            'success' => true,
            'data' => ['user' => $user]
        ]);
    }

    private function issueToken(int $uid, string $role): string
    {
        $ttl = (int)($_ENV['JWT_TTL_SECONDS'] ?? 604800);

        $payload = [
            'uid' => $uid,
            'role' => $role,
            'iat' => time(),
            'exp' => time() + $ttl
        ];

        return Jwt::encode($payload, $_ENV['JWT_SECRET'] ?? '');
    }

    // FORGOT PASSWORD - SENDS VERIFICATION CODE
    public function forgotPassword(): void
    {
        $body = json_decode(file_get_contents('php://input'), true) ?? [];
        $email = trim(strtolower($body['email'] ?? ''));

        // Validate email
        if (!$email) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'Email is required'
                ]
            ], 422);
            return;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'Invalid email format'
                ]
            ], 422);
            return;
        }

        // Check if user exists
        $user = $this->users->findByEmail($email);
        if (!$user) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'EMAIL_NOT_FOUND',
                    'message' => 'Email not found'
                ]
            ], 404);
            return;
        }

        // User exists. Bypass email verification and OTP generation as requested by user.
        Response::json([
            'success' => true,
            'message' => 'Email verified. Proceed to reset password.',
            'data' => []
        ]);
    }

    // RESET PASSWORD WITH VERIFICATION CODE
    public function resetPassword(): void
    {
        $body = json_decode(file_get_contents('php://input'), true) ?? [];

        $email = trim(strtolower($body['email'] ?? ''));
        $code = trim($body['code'] ?? '');
        $newPass = (string)($body['password'] ?? '');

        // Validations
        if (!$email) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'Email is required'
                ]
            ], 422);
            return;
        }

        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'Invalid email format'
                ]
            ], 422);
            return;
        }

        if (strlen($newPass) < 8) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'Password must be at least 8 characters long'
                ]
            ], 422);
            return;
        }

        // Check user exists
        $user = $this->users->findByEmail($email);
        if (!$user) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_EMAIL',
                    'message' => 'Email not found'
                ]
            ], 404);
            return;
        }

        // OTP Verification bypassed as per request
        // if (!$this->users->verifyResetToken($email, $code)) {
        //     ...
        // }

        // Update password (this will also clear the reset token in the model)
        $this->users->updatePassword(
            (int)$user['id'],
            password_hash($newPass, PASSWORD_BCRYPT)
        );

        Response::json([
            'success' => true,
            'message' => 'Password updated successfully'
        ]);
    }

    // CHANGE PASSWORD (LOGGED USERS)
    public function changePassword(): void
    {
        $auth = $_SERVER['auth'] ?? [];
        $uid = (int)($auth['uid'] ?? 0);

        if ($uid <= 0) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'UNAUTHORIZED',
                    'message' => 'Authentication required'
                ]
            ], 401);
            return;
        }

        $body = json_decode(file_get_contents('php://input'), true) ?? [];

        $oldPassword = (string)($body['old_password'] ?? '');
        $newPassword = (string)($body['new_password'] ?? '');

        // Validation
        if (!$oldPassword || !$newPassword) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'Old and new passwords required'
                ]
            ], 422);
            return;
        }

        if (strlen($newPassword) < 6) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'VALIDATION',
                    'message' => 'New password must be at least 6 characters long'
                ]
            ], 422);
            return;
        }

        $user = $this->users->findById($uid);

        if (!$user || !password_verify($oldPassword, $user['password_hash'])) {
            Response::json([
                'success' => false,
                'error' => [
                    'code' => 'INVALID_PASSWORD',
                    'message' => 'Incorrect current password'
                ]
            ], 401);
            return;
        }

        $this->users->updatePassword(
            $uid,
            password_hash($newPassword, PASSWORD_BCRYPT)
        );

        Response::json([
            'success' => true,
            'message' => 'Password changed successfully'
        ]);
    }
}
