<?php
header('Content-Type: application/json; charset=utf-8');

require 'conexion.php';

// Se verifica si la App envió el ID de la cancha
if (isset($_GET['cancha_id'])) {
    $cancha_id = $_GET['cancha_id'];

    try {
        // Consulta SQL con ordenamiento natural 
        $sql = "SELECT id, descriptor, norte, este, cota, fecha_creacion, fecha_modificacion
                FROM puntos_referencia
                WHERE cancha_id = :cancha_id
                ORDER BY LENGTH(descriptor) ASC, descriptor ASC";

        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':cancha_id', $cancha_id, PDO::PARAM_INT);
        $stmt->execute();

        $prs = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Devolver los puntos de referencia como JSON
        echo json_encode($prs);

    } catch(PDOException $e) {
        // En caso de error, devolver un mensaje de error
        echo json_encode(['error' => 'Error en BD: ' . $e->getMessage()]);
    }
} else {
    echo json_encode(['error' => 'No se proporcionó el ID de la cancha']);
}

?>