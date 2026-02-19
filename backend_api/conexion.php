<?php
// conexion.php
$host = "localhost";
$db   = "topografia_db"; // El nombre exacto de la base de datos en phpMyAdmin
$user = "root";          // Usuario por defecto de XAMPP
$pass = "";              // XAMPP trae la contraseña vacía por defecto

try {
    $pdo = new PDO("mysql:host=$host;dbname=$db;charset=utf8", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    // Si no dice nada, es que conectó bien.
} catch (PDOException $e) {
    die("Error de conexión: " . $e->getMessage());
}
?>