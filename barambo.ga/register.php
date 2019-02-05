<?php include('./header.php');$title = "Login System"; ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><?php echo $title ?></title>
<link href="style.css" rel="stylesheet" type="text/css" />
</head>

<body>
<div id="container">
<center>
<?php
session_start();
if(isset($_SESSION['username'])){
  if(get_magic_quotes_gpc()) {
      $_GET = strip_slashes_deep($_GET);
      $_POST = strip_slashes_deep($_POST);
  }

  function strip_slashes_deep($data) {
      if(is_array($data)) {
          foreach ($data as $key => $value) {
              $data[$key] = strip_slashes_deep($value);
          }
          return $data;
      }
      else
      {
  		return stripslashes($data);
      }
  }
  $nickname = "";
  $username = "";
  if (isset($_POST['submit'])) {

  	$submit = $_POST['submit'];
  	//$nickname = $_POST['nickname'];
  	$username = $_POST['username'];
  	$password = $_POST['password'];
  	$password2 = $_POST['password2'];
  	$date = date("Y-m-d");

  	if(strlen($username) > 25){
  		echo "<font color=red>Maximum Limit for Username is 25 characters!</font>";
  	}
  	if ($username&&$password&&$password2){
          if($password == $password2){
  			if(strlen($password) > 25 || strlen($password) < 6){
  				echo "<font color=red>Password must be between 6 - 25 characters!</font>";
  			}
  			else {
                $teller = 0;
                $connection = mysqli_connect('localhost', 'root', 'itworks') or die ("Could not connect to the database server!");
                mysqli_select_db($connection, "barambo") or die ("Could not connect to the database");
                $query = "SELECT * FROM users WHERE username = '$username'";
                $keuring = mysqli_query($connection, $query);
                while (mysqli_fetch_assoc($keuring)){
                    $teller = $teller + 1;
                }
                if($teller === 1){
                    echo "<font color=red>Username already exists.</font>";
                }
                else{
      				//encrypt password
      				$password = sha1($password);
      				$password2 = sha1($password2);

      				//Register the user!
      				$connection = mysqli_connect('localhost', 'root', 'itworks') or die ("Could not connect to the database server!");
      				mysqli_select_db($connection, "barambo") or die ("Could not connect to the database");

      				$register = sprintf("INSERT INTO users (username, password, date, nickname, email) VALUES('%s', '%s', '%s', '$username', '')",
      				mysqli_real_escape_string($connection, $username),
      				mysqli_real_escape_string($connection, $password),
      				mysqli_real_escape_string($connection, $date));
                    
      				if ($connection->query($register) === TRUE) {
      					die("Successfully registered please <a href='index.php'>Log in</a>!");
      				} else {
                        //print($register);
      					die("Error");
      				}
                }
  			}
          }
          else
              echo "<font color=red>Password does not match!</font>";
      }
      else
          echo "<font color=red>Please fill in all fields!</font>";
  }
  ?>
      <h1>Register</h1>

      <form action="register.php" method="post" id="register">
          <fieldset>
              <p>Username: <input name="username" type="text" value="<?php echo $username; ?>" size="25" maxlength="25" /></p>
              <p>Password: <input name="password" type="password" size="25" maxlength="25" /></p>
              <p>Repeat Password: <input name="password2" type="password" size="25" maxlength="25" /></p>
              <!--<p>Nick Name: <input name="nickname" type="text" value="<?php echo $nickname; ?>" size="25" maxlength="25" /></p>-->
              <input name="submit" type="submit" value="Register" />
          </fieldset>
  <?php
  if(isset($_SESSION['username'])){
      echo "<p><a href='home.php'>Back to home page!</a></p>";
  }
  ?>
</form><?php
}
else{
  header('Location: '. './');
}
   ?>
</center>
</div>
</body>
</html>
