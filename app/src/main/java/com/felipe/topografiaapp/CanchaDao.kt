package com.felipe.topografiaapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CanchaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCanchas(canchas: List<Cancha>)

    @Query("SELECT * FROM tabla_canchas WHERE codigo_fundo = :codigoFundo")
    suspend fun obtenerCanchasPorFundo(codigoFundo: String): List<Cancha>
}