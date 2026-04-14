<?php
header('Content-Type: application/json'); // Importante: Le dice al cliente que esto es JSON
require 'conexion.php';

if (!isset($_GET['codigo_fundo'])) {
    echo json_encode(["error" => "Falta especificar el codigo_fundo"]);
    exit;
}

$codigo = $_GET['codigo_fundo'];

try {
    $sql = "SELECT 
                c.id, 
                c.codigo_fundo, 
                f.nombre_fundo, 
                c.numero_cancha, 
                c.fecha_creacion, 
                c.fecha_actualizacion 
            FROM canchas c
            INNER JOIN fundos f ON c.codigo_fundo = f.codigo_fundo
            WHERE c.codigo_fundo = :codigo";
            
    $stmt = $pdo->prepare($sql);
    $stmt->bindParam(':codigo', $codigo, PDO::PARAM_STR);
    $stmt->execute();
    
    $canchas = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (empty($canchas)) {
        echo json_encode(["mensaje" => "Aún no hay canchas registradas para este fundo."]);
    } else {
        echo json_encode($canchas);
    }

} catch (PDOException $e) {
    echo json_encode(["error" => "Error en la base de datos: " . $e->getMessage()]);
}
?>