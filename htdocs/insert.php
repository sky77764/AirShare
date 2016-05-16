<?php  
$servername = "localhost";
$username = "root";
$password = "0000";

$link = mysql_connect($servername, $username, $password);
if (!$link) {
    die('Not connected : ' . mysql_error());
}

$db_selected = mysql_select_db('airshare', $link);
if (!$db_selected) {
    die ('Can\'t use airshare : ' . mysql_error());
}

$sql_test    = "SELECT count(*) FROM LOCATION where username = '$_GET[username]'";
$result = mysql_query($sql_test, $link);
$row = mysql_fetch_array($result);
//echo "$row[0]/";
if($row[0] == "0") {
	$sql = "INSERT INTO location values ('$_GET[username]', $_GET[latitude], $_GET[longitude], current_timestamp)";
}
else {
	$sql = "update location 
	set latitude = $_GET[latitude], longitude = $_GET[longitude], time = current_timestamp 
	where username = '$_GET[username]'";
}

$result = mysql_query($sql, $link);
echo "$result";
   
mysql_close($link);  
?>  