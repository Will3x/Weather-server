<?php include('./header.php');$title = "Home"; ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title><?php echo $title ?></title>
  <link href="style.css" rel="stylesheet" type="text/css" />
</head>
<body>

  <?php
  include 'authorize.php';
  include 'functions.php';
  ?>

  <div id="container">
    <center>
      <div id="tempdata">
        <?php
        echo "<pre>";
        $myfile = fopen("./temp/lowestmaxtemps.txt", "r");
        $top10 = fread($myfile,filesize("./temp/lowestmaxtemps.txt"));
        $top10 = preg_split("/\*/", $top10);
        fclose($myfile);
        echo "<p>Welcome <b>". ucfirst($_SESSION['username']) . "</b>.</p>";
        ?>
        <h3>Top 10 places in Russia with the lowest maximum temperatures</h3>
        <table>
          <tr>
            <th>Nr</th>
			<th>Station number</th>
            <th>Location</th>
            <th> maximum measured temperature </th>
          </tr>
          <?php
          $teller = 0;
          for($i = 1; $i <= 10; $i++){
            ?>
            <tr>
              <td><?php echo $i;?></td>
              <td><?php echo $top10[$teller]; $teller++;?></td>
              <td><?php echo $top10[$teller]; $teller++;?></td>
              <td><?php echo $top10[$teller]; $teller++;?></td>
            </tr>
            <?php
          }
          ?>
        </table>
        <?php

        include 'cloudcoverage.php';

        ?>
      </div>
      <form action="./download.php" method="post">
          <input type="date" id="start" name="date"
                value="<?php echo date("Y-m-d");?>"
                min="<?php echo date("Y-m-d", strtotime("-4 week"));?>"
                max="<?php echo date("Y-m-d");?>">

          <input type="time" name="time"
                value="<?php echo date("H:i", strtotime("+4 hour"));?>">

          <input name="download" type="submit" value="Download" />
     </form>
     <a href="register.php"><p style="text-align:center">Register a new account here</p></a>
     <a href="logout.php"><p style="text-align:center">Logout</p></a>
   </center>
 </div>

 <!-- Refresh div with id="container" every 10 seconds -->
 <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
 <script type="text/javascript">

  setInterval(function () {
    $("#tempdata").load(" #tempdata");
}, 3000);

</script>
</body>
</html>
