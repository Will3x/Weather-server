<?php include('./header.php'); $title = "Login System"; ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title><?php echo $title ?></title>
  <link href="style.css" rel="stylesheet" type="text/css" />
</head>
<body>
  <div id="container">

    <?php
    session_start();
    if(isset($_SESSION['username'])){
      header('Location: '. './home.php');
    }

    elseif(isset($_POST['username']) && isset($_POST['password'])){
      $username = $_POST['username'];
      $password = sha1($_POST['password']);
      $connect = mysqli_connect('localhost', 'root', 'itworks') or die ('Couldn\'t Connect');
      mysqli_select_db($connect, "barambo") or die ("Couldn\'t Find your database !");

      $query = sprintf("SELECT id, username, password, nickname, date, email FROM users WHERE `username`='%s' AND `password`='%s'",
        mysqli_real_escape_string($connect, $username),
        mysqli_real_escape_string($connect, $password));

      $rows = mysqli_query($connect, $query);

      $numrows = mysqli_num_rows($rows);
      if($numrows)
      {
        while($row = mysqli_fetch_assoc($rows))
        {
          $dbusername = $row['username'];
          $dbpassword = $row['password'];
          $dbnickname = $row['nickname'];
          $dbdate = $row['date'];
          $dbemail = $row['email'];
        }
        header('Location: '. './home.php');
        echo("<p>Welcome back $dbusername !</p>");
          //echo("<p>Your Email address: $dbemail</p>");
        echo("<a href='logout.php'>Logout</a>");

        $_SESSION['username'] = $dbusername;
        $_SESSION["count"] = 0;
        $_SESSION['array'] = array();
      }
      else
        echo("Username or Password is wrong!");
    }
    else {
      header('Location: '. './');
    }
    ?>
  </div>
</body>
</html>
