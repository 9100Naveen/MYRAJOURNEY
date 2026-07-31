<?php
declare(strict_types=1);

namespace Src\Models;

use PDO;
use Src\Config\DB;

class UserModel
{
    private PDO $db;

    public function __construct()
    {
        $this->db = DB::conn();
    }

    public function findByEmail(string $email): ?array
    {
        $email = strtolower(trim($email));

        $stmt = $this->db->prepare(
            'SELECT * FROM users WHERE email = :email LIMIT 1'
        );
        $stmt->execute([':email' => $email]);

        $row = $stmt->fetch();
        return $row ?: null;
    }

    public function create(array $data): int
    {
        $stmt = $this->db->prepare(
            'INSERT INTO users
                (email, phone, password_hash, role, name, avatar_url, status, created_at, updated_at)
             VALUES
                (:email, :phone, :password_hash, :role, :name, :avatar_url, :status, NOW(), NOW())'
        );

        $stmt->execute([
            ':email'        => strtolower(trim($data['email'])),
            ':phone'        => $data['phone'] ?? null,
            ':password_hash'=> $data['password_hash'],
            ':role'         => $data['role'],
            ':name'         => $data['name'] ?? null,
            ':avatar_url'   => $data['avatar_url'] ?? null,
            ':status'       => 'ACTIVE',
        ]);

        return (int)$this->db->lastInsertId();
    }

    public function findById(int $id): ?array
    {
        $stmt = $this->db->prepare(
            'SELECT id, email, phone, role, name, avatar_url, status,
                    last_login_at, created_at, updated_at
             FROM users WHERE id = :id'
        );
        $stmt->execute([':id' => $id]);

        $row = $stmt->fetch();
        return $row ?: null;
    }

    public function setLastLogin(int $id): void
    {
        $this->db->prepare(
            'UPDATE users SET last_login_at = NOW() WHERE id = :id'
        )->execute([':id' => $id]);
    }

    public function updateMe(int $id, array $data): void
    {
        $stmt = $this->db->prepare(
            'UPDATE users
             SET name = :name, phone = :phone, updated_at = NOW()
             WHERE id = :id'
        );

        $stmt->execute([
            ':name'  => $data['name'] ?? null,
            ':phone' => $data['phone'] ?? null,
            ':id'    => $id
        ]);
    }

    public function updatePassword(int $id, string $passwordHash): void
    {
        $stmt = $this->db->prepare(
            'UPDATE users
             SET password_hash = :password_hash, updated_at = NOW(), reset_code = NULL, reset_expires_at = NULL
             WHERE id = :id'
        );

        $stmt->execute([
            ':password_hash' => $passwordHash,
            ':id'            => $id
        ]);
    }

    public function saveResetToken(string $email, string $code, string $expiresAt): void
    {
        $stmt = $this->db->prepare(
            'UPDATE users
             SET reset_code = :code, reset_expires_at = :expiresAt
             WHERE email = :email'
        );

        $stmt->execute([
            ':code'      => $code,
            ':expiresAt' => $expiresAt,
            ':email'     => $email
        ]);
    }

    public function verifyResetToken(string $email, string $code): bool
    {
        $stmt = $this->db->prepare(
            'SELECT id FROM users 
             WHERE email = :email 
             AND reset_code = :code 
             AND reset_expires_at >= NOW() 
             LIMIT 1'
        );

        $stmt->execute([
            ':email' => $email,
            ':code'  => $code
        ]);

        return $stmt->fetch() !== false;
    }
}
