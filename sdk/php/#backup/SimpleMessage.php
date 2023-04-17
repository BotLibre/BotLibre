<?php
$url = "https://www.botlibre.com/rest/api/check-user";
$ch = curl_init();
$xmlData = "<user user=\"Account_test\" application=\"986530966192349057\" password=\"password123\"></user>";
$xml = simplexml_load_string($xmlData) or die("Error: Cannot create object");
print_r($xml);// print_r is used to display the contenet of a variable.

curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $xmlData);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$headers = [
    'Content-Type: application/xml',
    'Accept: application/xml'
];

curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);

$response = curl_exec($ch);
if ($e = curl_error($ch)) {
    echo $e;
} else {
    echo "<br>";
    $result = simplexml_load_string($response);
    print_r($result);
    // echo "<br>";
    // //echo "Data: " . $response;
    // echo "<br>";
    // echo "Message: " . $result->message;
    // echo "<br>";
    // echo "Application Number: " . $result->attributes()->conversation;
}

curl_close($ch);
?>