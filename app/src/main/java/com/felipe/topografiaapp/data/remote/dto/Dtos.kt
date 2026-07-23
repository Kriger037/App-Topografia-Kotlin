package com.felipe.topografiaapp.data.remote.dto

data class FundoDto(
    val id: Int,
    val codigo_fundo: String,
    val nombre_fundo: String,
    val comuna: String?
)

data class CanchaDto(
    val id: Int,
    val codigo_fundo: String,
    val nombre_fundo: String?,
    val numero_cancha: String,
    val huso: String = "18",
    val fecha_creacion: String,
    val fecha_actualizacion: String
)

data class PRDto(
    val id: Int,
    val canchaId: Int,
    val descriptor: String,
    val norte: Double,
    val este: Double,
    val cota: Double,
    val latitud: Double?,
    val longitud: Double?,
    val fecha_creacion: String,
    val fecha_modificacion: String
)

data class ConversionResponse(
    val exito: Boolean,
    val latitud: Double?,
    val longitud: Double?,
    val norte: Double?,
    val este: Double?,
    val zona: Int?,
    val mensaje: String?
)

data class PRSyncDto(
    val canchaId: Int,
    val descriptor: String,
    val norte: Double,
    val este: Double,
    val cota: Double,
    val latitud: Double?,
    val longitud: Double?
)

data class ActualizarPRsRequest(
    val prs: List<PRSyncDto>
)

data class ActualizarPRsResponse(
    val exito: Boolean,
    val actualizados: Int,
    val insertados: Int,
    val errores: Int,
    val mensaje: String?
)

data class EliminarFundoRequest(val codigo_fundo: String)
data class EliminarCanchaRequest(val cancha_id: Int)
data class EliminarPRRequest(val pr_id: Int)

data class EliminarResponse(
    val exito: Boolean,
    val mensaje: String?
)
