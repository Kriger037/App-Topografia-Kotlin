<?php
header('Content-Type: application/json; charset=utf-8'); // Importante: Le dice al cliente que esto es JSON
require_once 'conexion.php';

// Datos recibidos desde el formulario de login en Android
$usuario_ingresado = $_POST['usuario'] ?? '';
$contrasena_ingresada = $_POST['contrasena'] ?? '';

// Validacion de que datos no esten vacios
if (empty($usuario_ingresado) || empty($contrasena_ingresada)){
    echo json_encode(["exito" => false, "mensaje" => "Por favor, complete todos los campos."]);
    exit;
}

try{
    // Consulta para verificar la existencia del usuario y contraseña
    $query = "SELECT nombre FROM usuarios WHERE usuario = :usuario AND contraseña = :contrasena";
    $stmt = $pdo->prepare($query);
    $stmt->bindParam(':usuario', $usuario_ingresado);
    $stmt->bindParam(':contrasena', $contrasena_ingresada);
    $stmt->execute();

    if ($stmt->rowCount() > 0){
        // Al encontrar un usuario valido, se obtiene su nombre para enviarlo en la respuesta
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        echo json_encode([
            "exito" => true,
            "mensaje" => "Login exitoso.",
            "nombre_usuario" => $row['nombre'],
        ]);
    } else {
        // Si no se encuentra un usuario valido, se devuelve un mensaje de error
        echo json_encode(["exito" => false, "mensaje" => "Usuario o contraseña incorrectos."]);
    }
} catch (PDOException $e) {
    echo json_encode(["exito" => false, "mensaje" => "Error de base de datos: " . $e->getMessage()]);
}

?>