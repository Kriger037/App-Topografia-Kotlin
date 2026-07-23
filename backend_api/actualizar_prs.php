<?php
require_once 'conexion.php';

header('Content-Type: application/json');

$datos = json_decode(file_get_contents('php://input'), true);

if (!isset($datos['prs']) || !is_array($datos['prs'])) {
    echo json_encode(['exito' => false, 'mensaje' => 'Datos inválidos']);
    exit;
}

$actualizados = 0;
$insertados = 0;
$errores = 0;

try {
    $pdo->beginTransaction();

    $sqlCheck = "SELECT id FROM puntos_referencia WHERE cancha_id = ? AND descriptor = ?";
    $stmtCheck = $pdo->prepare($sqlCheck);

    $sqlInsert = "INSERT INTO puntos_referencia 
                  (cancha_id, descriptor, norte, este, cota, latitud, longitud) 
                  VALUES (?, ?, ?, ?, ?, ?, ?)";
    $stmtInsert = $pdo->prepare($sqlInsert);

    $sqlUpdate = "UPDATE puntos_referencia 
                  SET norte = ?, este = ?, cota = ?, latitud = ?, longitud = ? 
                  WHERE cancha_id = ? AND descriptor = ?";
    $stmtUpdate = $pdo->prepare($sqlUpdate);

    foreach ($datos['prs'] as $pr) {
        $cancha_id  = intval($pr['canchaId'] ?? 0);
        $descriptor = trim($pr['descriptor'] ?? '');
        $norte      = floatval($pr['norte'] ?? 0);
        $este       = floatval($pr['este'] ?? 0);
        $cota       = floatval($pr['cota'] ?? 0);
        $latitud    = isset($pr['latitud']) ? floatval($pr['latitud']) : null;
        $longitud   = isset($pr['longitud']) ? floatval($pr['longitud']) : null;

        if ($cancha_id <= 0 || empty($descriptor) || $norte <= 0 || $este <= 0) {
            $errores++;
            continue;
        }

        $stmtCheck->execute([$cancha_id, $descriptor]);
        $existeId = $stmtCheck->fetchColumn();

        if ($existeId) {
            $stmtUpdate->execute([
                $norte, $este, $cota, $latitud, $longitud,
                $cancha_id, $descriptor
            ]);
            $actualizados++;
        } else {
            $stmtInsert->execute([
                $cancha_id, $descriptor, $norte, $este, $cota, $latitud, $longitud
            ]);
            $insertados++;
        }
    }

    $pdo->commit();

    echo json_encode([
        'exito'       => true,
        'actualizados' => $actualizados,
        'insertados'  => $insertados,
        'errores'     => $errores
    ]);

} catch (Exception $e) {
    $pdo->rollBack();
    echo json_encode(['exito' => false, 'mensaje' => $e->getMessage()]);
}
?>