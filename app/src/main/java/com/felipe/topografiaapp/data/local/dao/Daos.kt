package com.felipe.topografiaapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.felipe.topografiaapp.data.local.entity.CanchaEntity
import com.felipe.topografiaapp.data.local.entity.FundoEntity
import com.felipe.topografiaapp.data.local.entity.PREntity
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------------------------
// DAOs — ahora retornan Flow en lugar de listas simples para que los
// ViewModels puedan observar cambios reactivamente (Room + LiveData/Flow).
// ---------------------------------------------------------------------------

@Dao
interface FundoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFundos(fundos: List<FundoEntity>)

    @Query("SELECT * FROM tabla_fundos ORDER BY nombreFundo ASC")
    fun obtenerTodosFlow(): Flow<List<FundoEntity>>

    @Query("SELECT * FROM tabla_fundos")
    suspend fun obtenerTodos(): List<FundoEntity>
}

@Dao
interface CanchaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCanchas(canchas: List<CanchaEntity>)

    @Query("SELECT * FROM tabla_canchas WHERE codigoFundo = :codigoFundo ORDER BY numeroCancha ASC")
    fun obtenerPorFundoFlow(codigoFundo: String): Flow<List<CanchaEntity>>

    @Query("SELECT * FROM tabla_canchas WHERE codigoFundo = :codigoFundo")
    suspend fun obtenerPorFundo(codigoFundo: String): List<CanchaEntity>

    // Recupera el huso de una cancha específica (necesario para conversión UTM)
    @Query("SELECT huso FROM tabla_canchas WHERE id = :canchaId LIMIT 1")
    suspend fun obtenerHuso(canchaId: Int): Int?
}

@Dao
interface PRDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPRs(prs: List<PREntity>)

    @Query("SELECT * FROM tabla_prs WHERE canchaId = :canchaId ORDER BY LENGTH(descriptor) ASC, descriptor ASC")
    fun obtenerPorCanchaFlow(canchaId: Int): Flow<List<PREntity>>

    @Query("SELECT * FROM tabla_prs WHERE canchaId = :canchaId")
    suspend fun obtenerPorCancha(canchaId: Int): List<PREntity>

    // Para el SyncWorker: traer todos los PRs pendientes de sincronizar
    @Query("SELECT * FROM tabla_prs WHERE isDirty = 1")
    suspend fun obtenerPendientesSincronizacion(): List<PREntity>

    // Marcar todos los PRs de una cancha como sincronizados
    @Query("UPDATE tabla_prs SET isDirty = 0, lastSyncAt = :timestamp WHERE canchaId = :canchaId")
    suspend fun marcarComoSincronizados(canchaId: Int, timestamp: Long = System.currentTimeMillis())

    // Insertar PRs importados localmente (marcados como dirty para sync posterior)
    @Query("UPDATE tabla_prs SET latitud = :lat, longitud = :lng WHERE id = :prId")
    suspend fun actualizarCoordenadas(prId: Int, lat: Double, lng: Double)
}
