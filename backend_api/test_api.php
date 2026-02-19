<?php
// test_api.php
header('Content-Type: application/json'); // Importante: Le dice al cliente que esto es JSON
require 'conexion.php';

// Hacemos la consulta
$sql = "SELECT * FROM usuarios";
$stmt = $pdo->prepare($sql);
$stmt->execute();
$usuarios = $stmt->fetchAll(PDO::FETCH_ASSOC);

// Convertimos los datos a JSON y los enviamos
echo json_encode($usuarios);
?>