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
