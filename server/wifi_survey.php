<?php

$resp = file_get_contents('php://input'); //print_r($_POST, true);
echo "Request recieved, length: " . strlen($resp) . "\n";

$fd = fopen("wifi_survey.log", "a");
flock($fd, LOCK_EX);
fwrite($fd, $resp . "\n");
flock($fd, LOCK_UN);
fclose($fd);

echo "Ok\n";
?>
