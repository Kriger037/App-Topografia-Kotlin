<?php
header('Content-Type: application/json; charset=utf-8'); // importante para que JS interprete que esto es JSON

require_once 'conexion.php'; // Conexión a la base de datos

if (isset($_GET['codigo_fundo']) && !empty($_GET['codigo_fundo'])) {
    $codigo_fundo = $_GET['codigo_fundo'];

    try {
        $sql = "SELECT id, numero_cancha FROM canchas WHERE codigo_fundo = :codigo ORDER BY id ASC";
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':codigo', $codigo_fundo, PDO::PARAM_STR);
        $stmt->execute();
        
        $canchas = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode($canchas);

    } catch (PDOException $e) {
        echo json_encode(["error" => "Error de BD: " . $e->getMessage()]);
    }
} else {
    echo json_encode([]);
}
?>