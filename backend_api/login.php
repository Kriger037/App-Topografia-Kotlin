<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'conexion.php';

$usuario_ingresado  = $_POST['usuario'] ?? '';
$contrasena_ingresada = $_POST['contrasena'] ?? '';

if (empty($usuario_ingresado) || empty($contrasena_ingresada)) {
    echo json_encode(["exito" => false, "mensaje" => "Por favor, complete todos los campos."]);
    exit;
}

try {
    $query = "SELECT nombre, rol FROM usuarios WHERE usuario = :usuario AND contraseña = :contrasena";
    $stmt  = $pdo->prepare($query);
    $stmt->bindParam(':usuario',    $usuario_ingresado);
    $stmt->bindParam(':contrasena', $contrasena_ingresada);
    $stmt->execute();

    if ($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        echo json_encode([
            "exito"         => true,
            "mensaje"       => "Login exitoso.",
            "nombre_usuario" => $row['nombre'],
            "rol"           => $row['rol']
        ]);
    } else {
        echo json_encode(["exito" => false, "mensaje" => "Usuario o contraseña incorrectos."]);
    }
} catch (PDOException $e) {
    echo json_encode(["exito" => false, "mensaje" => "Error de base de datos: " . $e->getMessage()]);
}
?>