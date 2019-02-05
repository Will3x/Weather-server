<?php

function getFileCount() {
	$fileCount = 0;

	$iterator = new RecursiveIteratorIterator(new RecursiveDirectoryIterator("weer"), RecursiveIteratorIterator::CHILD_FIRST);
	foreach ($iterator as $fileinfo) {
		if ($fileinfo->isFile()) {
			$fileCount++;
		}
	}
    return $fileCount;
}

function sortOnCountry ($array, $key) {
    $sorter=array();
    $ret=array();
    reset($array);
    foreach ($array as $ii => $va) {
        $sorter[$ii]=$va[$key];
    }
    asort($sorter);
    foreach ($sorter as $ii => $va) {
        $ret[$ii]=$array[$ii];
    }
    $array=$ret;
}

?>