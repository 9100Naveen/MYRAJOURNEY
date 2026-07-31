<?php
declare(strict_types=1);

namespace Src\Controllers;

use Src\Models\SettingsModel;
use Src\Utils\Response;

class SettingsController
{
	private SettingsModel $settings;
	public function __construct(){ $this->settings = new SettingsModel(); }

	public function getMine(): void
	{
		$auth=$_SERVER['auth'] ?? [];
		$uid=(int)($auth['uid'] ?? 0);
		$data=$this->settings->getAll($uid);
		Response::json(['success'=>true,'data'=>$data]);
	}

	public function putMine(): void
	{
		$auth=$_SERVER['auth'] ?? [];
		$uid=(int)($auth['uid'] ?? 0);
		$body=json_decode(file_get_contents('php://input'), true) ?? [];
		if (!isset($body['key'])) { Response::json(['success'=>false,'error'=>['code'=>'VALIDATION','message'=>'Missing key']],422); return; }
		$this->settings->put($uid, (string)$body['key'], isset($body['value']) ? (string)$body['value'] : null);
		Response::json(['success'=>true]);
	}
}




















