package com.felipe.topografiaapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.felipe.topografiaapp.data.local.dao.PRDao
import com.felipe.topografiaapp.data.remote.ApiService
import com.felipe.topografiaapp.data.remote.dto.ActualizarPRsRequest
import com.felipe.topografiaapp.data.remote.dto.PRSyncDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// ---------------------------------------------------------------------------
// SyncWorker — sincroniza los PRs con isDirty=true al servidor
// Se ejecuta automáticamente cuando hay conexión a internet
// ---------------------------------------------------------------------------

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val prDao: PRDao,
    private val apiService: ApiService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d("SyncWorker", "Iniciando sincronización de PRs pendientes")

            val prsPendientes = prDao.obtenerPendientesSincronizacion()

            if (prsPendientes.isEmpty()) {
                android.util.Log.d("SyncWorker", "No hay PRs pendientes de sincronizar")
                return Result.success()
            }

            android.util.Log.d("SyncWorker", "PRs pendientes: ${prsPendientes.size}")

            val dtos = prsPendientes.map { entity ->
                PRSyncDto(
                    canchaId = entity.canchaId,
                    descriptor = entity.descriptor,
                    norte = entity.norte,
                    este = entity.este,
                    cota = entity.cota,
                    latitud = entity.latitud,
                    longitud = entity.longitud
                )
            }

            val response = apiService.actualizarPRs(ActualizarPRsRequest(dtos))

            if (response.exito) {
                // Marcar todos los PRs sincronizados como limpios
                val canchaIds = prsPendientes.map { it.canchaId }.toSet()
                canchaIds.forEach { canchaId ->
                    prDao.marcarComoSincronizados(canchaId)
                }

                android.util.Log.d("SyncWorker",
                    "Sync exitosa: ${response.actualizados} actualizados, ${response.insertados} insertados")
                Result.success()
            } else {
                android.util.Log.e("SyncWorker", "Error en servidor: ${response.mensaje}")
                Result.retry()
            }

        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error en sync: ${e.message}", e)
            Result.retry()
        }
    }
}