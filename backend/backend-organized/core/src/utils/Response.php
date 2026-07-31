<?php
declare(strict_types=1);

namespace Src\Utils;

class Response
{
	public static function json($data, int $code = 200): void
	{
		http_response_code($code);
		echo json_encode($data, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
	}
}




















