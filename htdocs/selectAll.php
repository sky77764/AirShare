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

$sql    = 'SELECT * FROM LOCATION';
$res = mysql_query($sql, $link);

$result = array();  
   
while($row = mysql_fetch_array($res)){  
  array_push($result,  
    array('username'=>$row[0],'latitude'=>$row[1],'longitude'=>$row[2], 'time'=>$row[3]  
    ));  
}  
   
echo json_encode(array("result"=>$result));  
   
mysql_close($link);  
?>  