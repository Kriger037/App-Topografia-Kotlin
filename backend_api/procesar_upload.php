<?php
require_once 'conexion.php';
require_once 'vendor/autoload.php';

use proj4php\Proj4php;
use proj4php\Proj;
use proj4php\Point;

// Capturamos los datos iniciales
$fundo_codigo = $_POST['fundo_id'];
$cancha_id    = $_POST['cancha_id'] ?? 'NUEVO';
$tipo_nuevo   = $_POST['tipo_nuevo'] ?? 'CANCHA';

$huso_final = '18'; // Valor por defecto

$puntos_guardados = 0;
$puntos_actualizados = 0;
$puntos_ignorados = 0;

try {
    $pdo->beginTransaction();

    // =========================================================================
    // LÓGICA DE DETERMINACIÓN DE HUSO
    // =========================================================================

    if ($fundo_codigo === 'NUEVO') {
        $fundo_codigo = strtoupper(trim($_POST['nuevo_codigo']));
        $fundo_nombre = trim($_POST['nuevo_nombre']);
        
        $sqlNuevoFundo = "INSERT INTO fundos (codigo_fundo, nombre_fundo) VALUES (?, ?)";
        $stmt = $pdo->prepare($sqlNuevoFundo);
        $stmt->execute([$fundo_codigo, $fundo_nombre]);
    }

    if ($cancha_id === 'NUEVO') {
        // CASO 1: CANCHA NUEVA -> Tomamos el huso del formulario web y lo guardamos
        $huso_final = $_POST['huso_geografico'] ?? '18';
        $prefijo = ($tipo_nuevo === 'POZO') ? 'Pozo' : 'Cancha';
        
        $sqlMax = "SELECT numero_cancha FROM canchas WHERE codigo_fundo = ? AND numero_cancha LIKE ? ORDER BY id DESC LIMIT 1";
        $stmtMax = $pdo->prepare($sqlMax);
        $stmtMax->execute([$fundo_codigo, $prefijo . '%']);
        $ultima_cancha = $stmtMax->fetchColumn();

        if ($ultima_cancha) {
            preg_match('/(\d+)$/', $ultima_cancha, $coincidencias);
            $siguiente_numero = isset($coincidencias[1]) ? intval($coincidencias[1]) + 1 : 1;
            $nombre_cancha_final = $prefijo . ' ' . $siguiente_numero;
        } else {
            $nombre_cancha_final = $prefijo . ' 1';
        }

        // INSERTAMOS LA CANCHA INCLUYENDO SU HUSO
        $sqlNuevaCancha = "INSERT INTO canchas (codigo_fundo, numero_cancha, huso) VALUES (?, ?, ?)";
        $stmt = $pdo->prepare($sqlNuevaCancha);
        $stmt->execute([$fundo_codigo, $nombre_cancha_final, $huso_final]);
        $cancha_id = $pdo->lastInsertId();

    } else {
        // CASO 2: CANCHA EXISTENTE -> Ignoramos el formulario y le preguntamos a la BD
        $sqlHusoDB = "SELECT huso FROM canchas WHERE id = ?";
        $stmtHuso = $pdo->prepare($sqlHusoDB);
        $stmtHuso->execute([$cancha_id]);
        $huso_db = $stmtHuso->fetchColumn();
        
        if ($huso_db) {
            $huso_final = $huso_db;
        }
    }

    // =========================================================================
    // INICIALIZACIÓN GEODÉSICA
    // =========================================================================
    
    $proj4 = new Proj4php();
    $projUTM = new Proj("+proj=utm +zone={$huso_final} +south +ellps=WGS84 +datum=WGS84 +units=m +no_defs", $proj4);
    $projWGS84  = new Proj('EPSG:4326', $proj4);

    // =========================================================================
    // LECTURA DE ARCHIVO Y UPSERT
    // =========================================================================

    $archivo_tmp = $_FILES['archivo_puntos']['tmp_name'];
    $lineas = file($archivo_tmp, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);

    $sqlCheck = "SELECT id FROM puntos_referencia WHERE cancha_id = ? AND descriptor = ?";
    $stmtCheck = $pdo->prepare($sqlCheck);

    $sqlInsert = "INSERT INTO puntos_referencia (cancha_id, descriptor, norte, este, cota, latitud, longitud) VALUES (?, ?, ?, ?, ?, ?, ?)";
    $stmtInsert = $pdo->prepare($sqlInsert);

    $sqlUpdate = "UPDATE puntos_referencia SET norte = ?, este = ?, cota = ?, latitud = ?, longitud = ? WHERE id = ?";
    $stmtUpdate = $pdo->prepare($sqlUpdate);

    foreach ($lineas as $linea_actual) {
        $pedazos = preg_split('/[,\t;]+|\s+/', trim($linea_actual));
        
        $descriptor = "";
        $norte = 0;
        $este = 0;
        $cota = 0;
        $numeros_sobrantes = [];

        foreach ($pedazos as $pedazo) {
            $valor = trim($pedazo);
            if (empty($valor)) continue;

            if (preg_match('/[a-zA-Z]/', $valor)) {
                if (stripos($valor, 'PR') !== false) {
                    $descriptor = strtoupper($valor);
                }
            } elseif (is_numeric($valor) && $valor > 1000000) {
                $norte = floatval($valor);
            } elseif (is_numeric($valor) && $valor >= 100000 && $valor <= 999999) {
                $este = floatval($valor);
            } elseif (is_numeric($valor)) {
                $numeros_sobrantes[] = floatval($valor);
            }
        }

        if (count($numeros_sobrantes) > 0) {
            foreach ($numeros_sobrantes as $num) {
                if (strpos((string)$num, '.') !== false) {
                    $cota = $num;
                    break;
                }
            }
            if ($cota == 0) $cota = end($numeros_sobrantes);
        }

        if (!empty($descriptor) && $norte > 0 && $este > 0) {
            
            $puntoUTM = new Point($este, $norte, $projUTM);
            $puntoLatLong = $proj4->transform($projWGS84, $puntoUTM);
            $latitud = $puntoLatLong->y;
            $longitud = $puntoLatLong->x;

            $stmtCheck->execute([$cancha_id, $descriptor]);
            $pr_existente_id = $stmtCheck->fetchColumn();

            if ($pr_existente_id) {
                $stmtUpdate->execute([$norte, $este, $cota, $latitud, $longitud, $pr_existente_id]);
                $puntos_actualizados++;
            } else {
                $stmtInsert->execute([$cancha_id, $descriptor, $norte, $este, $cota, $latitud, $longitud]);
                $puntos_guardados++;
            }
        } else {
            $puntos_ignorados++;
        }
    }

    $pdo->commit();

    // Mensaje de éxito 
    echo "<div style='font-family: Arial; padding: 20px; background-color: #d4edda; color: #155724; border-radius: 5px; margin: 20px; text-align: center;'>";
    echo "<h2>¡Proceso Exitoso!</h2>";
    echo "<p>Huso Aplicado: <strong>Zona {$huso_final} Sur</strong></p>";
    echo "<p>Nuevos PRs guardados: <strong>$puntos_guardados</strong></p>";
    echo "<p>PRs existentes actualizados: <strong>$puntos_actualizados</strong></p>";
    echo "<p>Líneas ignoradas (basura): <strong>$puntos_ignorados</strong></p>";
    echo "<br><a href='formulario_upload.php' style='padding: 10px 20px; background: #28a745; color: white; text-decoration: none; border-radius: 5px;'>Subir otro levantamiento</a>";
    echo "</div>";

} catch (Exception $e) {
    $pdo->rollBack();
    die("<div style='color: white; background-color: #dc3545; font-family: Arial; padding: 20px; margin: 20px; border-radius: 5px;'><strong>Error Crítico:</strong> " . $e->getMessage() . "</div>");
}
?>