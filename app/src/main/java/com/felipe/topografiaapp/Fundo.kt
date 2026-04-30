package com.felipe.topografiaapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_fundos")
data class Fundo(
    @PrimaryKey val id: Int,
    val codigo_fundo: String,
    val nombre_fundo: String,
    val comuna: String?
)
