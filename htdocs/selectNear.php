<?php  

function distance($lat1, $lon1, $lat2, $lon2) {

  $theta = $lon1 - $lon2;
  $dist = sin(deg2rad($lat1)) * sin(deg2rad($lat2)) +  cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * cos(deg2rad($theta));
  $dist = acos($dist);
  $dist = rad2deg($dist);
  $miles = $dist * 60 * 1.1515;
  $unit = strtoupper($unit);

  return ($miles * 1.609344 * 1000);
}

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

$sql    = "SELECT latitude, longitude FROM LOCATION where username = '$_GET[username]'";
$result = mysql_query($sql, $link);
$cur_pos = mysql_fetch_array($result);



$sql    = "SELECT * FROM LOCATION where username <> '$_GET[username]'";
$res = mysql_query($sql, $link);

$result = array();  
   
while($row = mysql_fetch_array($res)){  
  $distance_meter = distance($cur_pos[0], $cur_pos[1], $row[1], $row[2]);
  if($distance_meter < 120) {
    array_push($result,  
    array('username'=>$row[0], 'latitude'=>$row[1],'longitude'=>$row[2], 'distance'=>$distance_meter));  
  }

    
}  
echo json_encode(array("result"=>$result));  

   
mysql_close($link);  
?>  


