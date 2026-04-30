package com.felipe.topografiaapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_prs")
data class PR(
    @PrimaryKey val id: Int,
    var cancha_id : Int = 0,
    val descriptor: String,
    val norte: Double,
    val este: Double,
    val cota: Double,
    val latitud: Double?,
    val longitud: Double?,
    val fecha_creacion: String,
    val fecha_modificacion: String
)
