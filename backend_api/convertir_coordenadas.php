<?php
require_once 'conexion.php';
require_once 'vendor/autoload.php';

use proj4php\Proj4php;
use proj4php\Proj;
use proj4php\Point;

header('Content-Type: application/json');

$norte = floatval($_GET['norte'] ?? 0);
$este  = floatval($_GET['este'] ?? 0);
$zona  = intval($_GET['zona'] ?? 18);

if ($norte <= 0 || $este <= 0) {
    echo json_encode(['exito' => false, 'mensaje' => 'Coordenadas inválidas']);
    exit;
}

try {
    $proj4    = new Proj4php();
    $projUTM  = new Proj("+proj=utm +zone={$zona} +south +ellps=WGS84 +datum=WGS84 +units=m +no_defs", $proj4);
    $projWGS84 = new Proj('EPSG:4326', $proj4);

    $puntoUTM     = new Point($este, $norte, $projUTM);
    $puntoLatLong = $proj4->transform($projWGS84, $puntoUTM);

    echo json_encode([
        'exito'    => true,
        'latitud'  => $puntoLatLong->y,
        'longitud' => $puntoLatLong->x,
        'norte'    => $norte,
        'este'     => $este,
        'zona'     => $zona
    ]);
} catch (Exception $e) {
    echo json_encode(['exito' => false, 'mensaje' => $e->getMessage()]);
}
?>