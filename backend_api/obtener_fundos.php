<?php
header('Content-Type: application/json'); // Importante: Le dice al cliente que esto es JSON
require 'conexion.php';

try{
    //Seleccionamos ID, Codigo y Nombre.
    $sql = "SELECT id, codigo_fundo, nombre_fundo, comuna FROM fundos";
    $stmt = $pdo->prepare($sql);
    $stmt->execute();
    $fundos = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode($fundos);

} catch (PDOException $e) {
    // Si hay un error, lo capturamos y enviamos un mensaje de error en formato JSON
    echo json_encode(['error' => "Error en la base de datos". $e->getMessage()]);
}

?>