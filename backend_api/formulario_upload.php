<?php
require_once 'conexion.php';

try {
    $sql_fundos = "SELECT id, codigo_fundo, nombre_fundo FROM fundos ORDER BY nombre_fundo ASC";
    $stmt_fundos = $pdo->prepare($sql_fundos);
    $stmt_fundos->execute();
    $fundos = $stmt_fundos->fetchAll(PDO::FETCH_ASSOC);
} catch (PDOException $e) {
    die("Error al cargar los fundos: " . $e->getMessage());
}
?>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Ingesta Topográfica - Ecofor</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { background-color: #f8f9fa; }
        .card { border: none; border-radius: 12px; }
        .bg-dark-custom { background-color: #212529; }
    </style>
</head>
<body>

<div class="container mt-5 mb-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow-lg">
                <div class="card-header bg-dark-custom text-white p-3">
                    <h4 class="mb-0 text-center">Gestión de Levantamientos (PRs)</h4>
                </div>
                <div class="card-body p-4">
                    
                    <form action="procesar_upload.php" method="POST" enctype="multipart/form-data">

                        <div class="mb-4">
                            <label class="form-label fw-bold">1. Ubicación del Fundo</label>
                            <select class="form-select" name="fundo_id" id="selectFundo" required>
                                <option value="">-- Seleccione un Fundo --</option>
                                <?php foreach ($fundos as $f): ?>
                                    <option value="<?= $f['codigo_fundo'] ?>">
                                        <?= $f['codigo_fundo'] ?> - <?= $f['nombre_fundo'] ?>
                                    </option>
                                <?php endforeach; ?>
                                <option value="NUEVO" class="text-success fw-bold">+ Registrar Nuevo Fundo</option>
                            </select>
                        </div>

                        <div id="divNuevoFundo" class="p-3 mb-4 rounded bg-light border" style="display: none;">
                            <h6 class="text-secondary mb-3">Datos del Fundo Nuevo</h6>
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <input type="text" class="form-control" name="nuevo_codigo" placeholder="Código (Ej: 1641)">
                                </div>
                                <div class="col-md-6">
                                    <input type="text" class="form-control" name="nuevo_nombre" placeholder="Nombre (Ej: Los Negros)">
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold">2. Identificación del Trabajo</label>
                            <select class="form-select" name="cancha_id" id="selectCancha" required disabled>
                                <option value="">-- Seleccione Fundo Primero --</option>
                            </select>

                            <div class="mt-3 p-3 border rounded bg-white shadow-sm" id="divNuevaCancha" style="display:none;">
                                <div class="row align-items-center">
                                    <div class="col-md-7">
                                        <label class="form-label d-block text-secondary small fw-bold">TIPO DE REGISTRO NUEVO:</label>
                                        <div class="form-check form-check-inline">
                                            <input class="form-check-input" type="radio" name="tipo_nuevo" value="CANCHA" checked>
                                            <label class="form-check-label small">Nueva Cancha</label>
                                        </div>
                                        <div class="form-check form-check-inline">
                                            <input class="form-check-input" type="radio" name="tipo_nuevo" value="POZO">
                                            <label class="form-check-label small">Nuevo Pozo</label>
                                        </div>
                                    </div>
                                    <div class="col-md-5 border-start">
                                        <label class="form-label d-block text-secondary small fw-bold">HUSO UTM:</label>
                                        <select class="form-select form-select-sm" name="huso_geografico">
                                            <option value="18" selected>18 Sur (Centro/Sur)</option>
                                            <option value="19">19 Sur (Cordillera/Extremo Sur)</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold">3. Archivo de Coordenadas (.txt o .csv)</label>
                            <input class="form-control" type="file" name="archivo_puntos" accept=".txt,.csv" required>
                        </div>

                        <hr>
                        <button type="submit" class="btn btn-success btn-lg w-100 fw-bold shadow">
                            Subir Levantamiento
                        </button>

                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.getElementById('selectFundo').addEventListener('change', function() {
        const divNuevoFundo = document.getElementById('divNuevoFundo');
        const selectCancha = document.getElementById('selectCancha');
        const divNuevaCancha = document.getElementById('divNuevaCancha');
        
        divNuevoFundo.style.display = 'none';
        selectCancha.disabled = true;
        divNuevaCancha.style.display = 'none';

        if (this.value === 'NUEVO') {
            divNuevoFundo.style.display = 'block';
            selectCancha.disabled = false;
            selectCancha.innerHTML = '<option value="NUEVO" selected>+ Crear Trabajo Nuevo</option>';
            divNuevaCancha.style.display = 'block';

        } else if (this.value) {
            fetch(`get_canchas_ajax.php?codigo_fundo=${this.value}`)
                .then(res => res.json())
                .then(data => {
                    selectCancha.disabled = false;
                    selectCancha.innerHTML = '<option value="">-- Seleccione Cancha Existente --</option>';
                    data.forEach(cancha => {
                        selectCancha.innerHTML += `<option value="${cancha.id}">${cancha.numero_cancha}</option>`;
                    });
                    selectCancha.innerHTML += '<option value="NUEVO" class="text-primary fw-bold">+ Crear Trabajo Nuevo</option>';
                })
                .catch(err => console.error("Error al cargar canchas:", err));
        } else {
            selectCancha.innerHTML = '<option value="">-- Seleccione Fundo Primero --</option>';
        }
    });

    document.getElementById('selectCancha').addEventListener('change', function() {
        document.getElementById('divNuevaCancha').style.display = (this.value === 'NUEVO') ? 'block' : 'none';
    });
</script>

</body>
</html>