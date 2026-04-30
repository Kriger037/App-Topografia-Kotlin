package com.felipe.topografiaapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PRDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPRs(prs: List<PR>)

    @Query("SELECT * FROM tabla_prs WHERE cancha_id = :canchaId")
    suspend fun obtenerPRsPorCancha(canchaId: Int): List<PR>
}