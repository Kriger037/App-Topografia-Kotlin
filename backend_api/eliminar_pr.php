<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'conexion.php';

$datos = json_decode(file_get_contents('php://input'), true);
$pr_id = intval($datos['pr_id'] ?? 0);

if ($pr_id <= 0) {
    echo json_encode(['exito' => false, 'mensaje' => 'ID de PR requerido']);
    exit;
}

try {
    $stmt = $pdo->prepare("DELETE FROM puntos_referencia WHERE id = ?");
    $stmt->execute([$pr_id]);

    if ($stmt->rowCount() > 0) {
        echo json_encode(['exito' => true, 'mensaje' => 'PR eliminado correctamente']);
    } else {
        echo json_encode(['exito' => false, 'mensaje' => 'PR no encontrado']);
    }
} catch (PDOException $e) {
    echo json_encode(['exito' => false, 'mensaje' => $e->getMessage()]);
}
?>