package com.felipe.topografiaapp.domain.model


data class Fundo(
    val id: Int,
    val codigoFundo: String,
    val nombreFundo: String,
    val comuna: String?
)

data class Cancha(
    val id: Int,
    val codigoFundo: String,
    val nombreFundo: String?,
    val numeroCancha: String,
    val huso: Int,                    // Zona UTM: 18 o 19 (Chile)
    val fechaCreacion: String,
    val fechaActualizacion: String
)

data class PR(
    val id: Int,
    val canchaId: Int,
    val descriptor: String,
    val norte: Double,
    val este: Double,
    val cota: Double,
    val latitud: Double?,
    val longitud: Double?,
    val fechaCreacion: String,
    val fechaModificacion: String,
    val isDirty: Boolean = false      // true = pendiente de sincronizar con servidor
)

// Resultado sellado de una conversión de coordenadas
sealed class CoordenadaResult {
    data class Exito(
        val latitud: Double,
        val longitud: Double,
        val norte: Double,
        val este: Double,
        val zona: Int
    ) : CoordenadaResult()

    data class Error(val mensaje: String) : CoordenadaResult()
}

// Tipos de formato detectados al importar un archivo
enum class FormatoArchivoCoord {
    UTM_ZONA_18,
    UTM_ZONA_19,
    LAT_LNG,
    DESCONOCIDO
}

// Resultado del parseo de un archivo de coordenadas
data class ResultadoImportacion(
    val puntosValidos: List<PR>,
    val lineasIgnoradas: Int,
    val formatoDetectado: FormatoArchivoCoord,
    val zonaDetectada: Int
)
