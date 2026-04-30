package com.felipe.topografiaapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_canchas")
data class Cancha(
    @PrimaryKey val id : Int,
    val codigo_fundo : String,
    val nombre_fundo: String?,
    val numero_cancha: String,
    val fecha_creacion: String,
    val fecha_actualizacion: String
)
