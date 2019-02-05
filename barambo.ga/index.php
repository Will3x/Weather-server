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
				echo "<p>Welcome <b>". ucfirst($_SESSION['username']) . "</b>.</p>";
				echo "<p><a href='home.php'>Click here to go back to the home page.</a></p>";
			}
			?>
			<h1>Login</h1>
			<form action="login.php" method="post">
				<p>Username: <input name="username" type="text" size="25" maxlength="25"/></p>
				<p>Password: <input name="password" type="password" size="25" maxlength="25" /></p>
				<p><input name="submit" type="submit" value="Log in" /></p>
				<?php
				if(isset($_SESSION['username'])){
					?>
					<a href="register.php">Register Account</a>
					<?php
				}?>
			</form>
		</center>
	</div>
</body>
</html>
