<?php
session_start();
unset($_SESSION['username']);

echo ("You have been successfully logged out! You will be redirected to the main page");
header('Location: ./');
?>