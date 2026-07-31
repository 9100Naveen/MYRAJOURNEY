<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\UserModel;
use Src\Utils\Response;

class UserController
{
    private UserModel $users;

    public function __construct()
    {
        $this->users = new UserModel();
    }

    public function updateMe(): void
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

        $this->users->updateMe($uid, $body);

        $user = $this->users->findById($uid);

        Response::json([
            'success' => true,
            'data' => ['user' => $user]
        ]);
    }
}
