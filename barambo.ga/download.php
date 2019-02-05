<?php

include 'authorize.php';
include 'functions.php';

if(isset($_POST["download"])){

	$fileCount = getFileCount();

	$index = $fileCount - 2;
	$secondMostRecentFile = "output".$index.".xml";
	$path = "weer/output".$index.".xml";

	header('Content-type: text/xml');
	header('Content-Disposition: attachment; filename="'.$secondMostRecentFile.'"');
	header("Content-Length: " . filesize($path));
	$fp = fopen($path, "r");
	fpassthru($fp);
	fclose($fp);
}
else{
	header('Location: '. './login.php');
}
?>
