package com.felipe.topografiaapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ---------------------------------------------------------------------------
// Entidades Room — versión 2 de la base de datos
// Cambios respecto a la versión 1:
//   FundoEntity   → agrega lastSyncAt
//   CanchaEntity  → agrega huso, lastSyncAt
//   PREntity      → agrega isDirty, lastSyncAt
//
// IMPORTANTE: estos cambios requieren una Migration 1→2 en AppDatabase.kt
// ---------------------------------------------------------------------------

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

@Entity(tableName = "tabla_prs")
data class PREntity(
    @PrimaryKey val id: Int,
    val canchaId: Int,
    val descriptor: String,
    val norte: Double,
    val este: Double,
    val cota: Double,
    val latitud: Double?,
    val longitud: Double?,
    val fechaCreacion: String,
    val fechaModificacion: String,
    val isDirty: Boolean = false,                      // pendiente de sync con servidor
    val lastSyncAt: Long = System.currentTimeMillis()
)
