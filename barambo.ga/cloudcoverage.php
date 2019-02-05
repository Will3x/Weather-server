<?php

$fileCount = getFileCount();

if ($fileCount > 1) {

    $index = $fileCount - 2;
    $secondMostRecentFile = "weer/output".$index.".xml";

    if (!isset($prevFile)) {
        $prevFile = "";

        if (file_exists($secondMostRecentFile) && $prevFile != $secondMostRecentFile) {
            $prevFile = $secondMostRecentFile;
            $xml = simplexml_load_file($secondMostRecentFile);

            if (($_SESSION["count"]) < $fileCount) {
                while ($_SESSION["count"] < $fileCount) {
                    $count = 0;
                    foreach($xml->children() as $data){
                        if($count > 1){
                            $country = (string)$data["country"];
                            $city = (string)$data["city"];
                            $STN = (string)$data->STN;
                            if (in_array($country, array('IRAQ', 'GEORGIA', 'AZERBAIJAN', 'ARMENIA'))) {
                                $cloud = (float)$data->CLDC;
                                if (array_key_exists($STN, $_SESSION['array'])) {
                                    $_SESSION['array'][$STN][2] += $cloud;
                                    $_SESSION['array'][$STN][3] ++;
                                    $_SESSION['array'][$STN][4] = $cloud;

                                } else{
                                    $_SESSION['array'][$STN] = array();
                                    array_push($_SESSION['array'][$STN], $country, $city, $cloud, 1, $cloud);
                                }
                            }
                        } elseif ($count == 0) {
                        # $datum = (string)$xml->DATE[$teller];
                        } else {
                            $_SESSION["time"] = (string)$xml->TIME;
                        }
                        $count++;
                    }
                    $_SESSION["count"]++;
                }
            } elseif (($_SESSION["count"]) > $fileCount) {
                $_SESSION["count"] = 0;
            }


            sortOnCountry($_SESSION['array'], 0);

            $countries = array('IRAQ', 'GEORGIA', 'AZERBAIJAN', 'ARMENIA');

            foreach ($countries as $k => $country) {
                echo "<h3>Cloud coverage in <b>" . ucfirst(strtolower($country)) . "</b></h3>
                <table>
                <tr>
                <th>#</th>
                <th>Location</th>
                <th>Cloud Coverage</th>
                <th>Cloud Coverage (avg.)</th>
                <th>Time</th>
                </tr>";
                $num = 1;
                foreach ($_SESSION['array'] as $x => $value) {
                    if ($value[0] == $country) {
                        echo "<tr>
                        <td>$num</td>
                        <td>" . ucfirst(strtolower($value[1])) . "</td>
                        <td>" . number_format($value[4], 1) . "</td>
                        <td>" . number_format(($value[2] / $value[3]), 1) . "</td>
                        <td>" . $_SESSION["time"] . "</td>";
                        $num++;
                    }
                }
            echo "</table>";
        }
    }
}
}
