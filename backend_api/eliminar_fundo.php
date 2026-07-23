<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'conexion.php';

$datos = json_decode(file_get_contents('php://input'), true);
$codigo_fundo = trim($datos['codigo_fundo'] ?? '');

if (empty($codigo_fundo)) {
    echo json_encode(['exito' => false, 'mensaje' => 'Código de fundo requerido']);
    exit;
}

try {
    // ON DELETE CASCADE elimina canchas y PRs automáticamente
    $stmt = $pdo->prepare("DELETE FROM fundos WHERE codigo_fundo = ?");
    $stmt->execute([$codigo_fundo]);

    if ($stmt->rowCount() > 0) {
        echo json_encode(['exito' => true, 'mensaje' => 'Fundo eliminado correctamente']);
    } else {
        echo json_encode(['exito' => false, 'mensaje' => 'Fundo no encontrado']);
    }
} catch (PDOException $e) {
    echo json_encode(['exito' => false, 'mensaje' => $e->getMessage()]);
}
?>