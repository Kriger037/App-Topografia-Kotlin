package com.felipe.topografiaapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index


@Entity(tableName = "tabla_fundos")
data class FundoEntity(
    @PrimaryKey val id: Int,
    val codigoFundo: String,
    val nombreFundo: String,
    val comuna: String?,
    val lastSyncAt: Long = System.currentTimeMillis()  // timestamp epoch ms
)

@Entity(tableName = "tabla_canchas")
data class CanchaEntity(
    @PrimaryKey val id: Int,
    val codigoFundo: String,
    val nombreFundo: String?,
    val numeroCancha: String,
    val huso: Int = 18,                                // Zona UTM: 18 o 19
    val fechaCreacion: String,
    val fechaActualizacion: String,
    val lastSyncAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tabla_prs",
    indices = [Index(value = ["canchaId", "descriptor"], unique = true)]
)
data class PREntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val canchaId: Int,
    val descriptor: String,
    val norte: Double,
    val este: Double,
    val cota: Double,
    val latitud: Double?,
    val longitud: Double?,
    val fechaCreacion: String,
    val fechaModificacion: String,
    val isDirty: Boolean = false,
    val lastSyncAt: Long = System.currentTimeMillis()
)
