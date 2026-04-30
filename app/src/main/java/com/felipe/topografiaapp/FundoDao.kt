package com.felipe.topografiaapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FundoDao {

    // Se guarda la lista de fundos que viene desde internet y reemplaza los repetidos (actualiza).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFundos(fundos : List<Fundo>)

    // Trae todos los fundos para mostrarlos en la pantalla principal
    @Query("SELECT * FROM tabla_fundos")
    suspend fun obtenerTodosLosFundos(): List<Fundo>
}