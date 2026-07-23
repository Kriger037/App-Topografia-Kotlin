<?php
header('Content-Type: application/json; charset=utf-8');
require_once 'conexion.php';

$datos = json_decode(file_get_contents('php://input'), true);
$cancha_id = intval($datos['cancha_id'] ?? 0);

if ($cancha_id <= 0) {
    echo json_encode(['exito' => false, 'mensaje' => 'ID de cancha requerido']);
    exit;
}

try {
    // ON DELETE CASCADE elimina los PRs automáticamente
    $stmt = $pdo->prepare("DELETE FROM canchas WHERE id = ?");
    $stmt->execute([$cancha_id]);

    if ($stmt->rowCount() > 0) {
        echo json_encode(['exito' => true, 'mensaje' => 'Cancha eliminada correctamente']);
    } else {
        echo json_encode(['exito' => false, 'mensaje' => 'Cancha no encontrada']);
    }
} catch (PDOException $e) {
    echo json_encode(['exito' => false, 'mensaje' => $e->getMessage()]);
}
?>