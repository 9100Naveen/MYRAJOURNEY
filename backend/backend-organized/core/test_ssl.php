<?php
$url = "https://openrouter.ai/api/v1/models";

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
// Try WITH ssl verification
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, true);

$response = curl_exec($ch);
if (curl_errno($ch)) {
    echo "SSL ON ERROR: " . curl_error($ch) . "\n";
} else {
    echo "SSL ON SUCCESS!\n";
}
curl_close($ch);

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
// Try WITHOUT ssl verification
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);

$response = curl_exec($ch);
if (curl_errno($ch)) {
    echo "SSL OFF ERROR: " . curl_error($ch) . "\n";
} else {
    echo "SSL OFF SUCCESS!\n";
}
curl_close($ch);
