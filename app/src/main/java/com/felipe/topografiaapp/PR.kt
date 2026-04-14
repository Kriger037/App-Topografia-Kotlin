package com.felipe.topografiaapp

data class PR(
    val id: Int,
    val descriptor: String,
    val norte: Double,
    val este: Double,
    val cota: Double,
    val latitud: Double?,
    val longitud: Double?,
    val fecha_creacion: String,
    val fecha_modificacion: String
)
