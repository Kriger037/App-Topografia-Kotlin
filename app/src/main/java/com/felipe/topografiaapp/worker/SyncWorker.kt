package com.felipe.topografiaapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.felipe.topografiaapp.data.local.dao.EliminacionPendienteDao
import com.felipe.topografiaapp.data.local.dao.PRDao
import com.felipe.topografiaapp.data.remote.ApiService
import com.felipe.topografiaapp.data.remote.dto.ActualizarPRsRequest
import com.felipe.topografiaapp.data.remote.dto.EliminarCanchaRequest
import com.felipe.topografiaapp.data.remote.dto.EliminarFundoRequest
import com.felipe.topografiaapp.data.remote.dto.EliminarPRRequest
import com.felipe.topografiaapp.data.remote.dto.PRSyncDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val prDao: PRDao,
    private val eliminacionPendienteDao: EliminacionPendienteDao,
    private val apiService: ApiService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // ---------------------------------------------------------------
            // PARTE 1: Sincronizar PRs con isDirty = true
            // ---------------------------------------------------------------
            android.util.Log.d("SyncWorker", "Iniciando sincronización de PRs pendientes")

            val prsPendientes = prDao.obtenerPendientesSincronizacion()

            if (prsPendientes.isNotEmpty()) {
                android.util.Log.d("SyncWorker", "PRs pendientes: ${prsPendientes.size}")

                val dtos = prsPendientes.map { entity ->
                    PRSyncDto(
                        canchaId   = entity.canchaId,
                        descriptor = entity.descriptor,
                        norte      = entity.norte,
                        este       = entity.este,
                        cota       = entity.cota,
                        latitud    = entity.latitud,
                        longitud   = entity.longitud
                    )
                }

                val response = apiService.actualizarPRs(ActualizarPRsRequest(dtos))

                if (response.exito) {
                    val canchaIds = prsPendientes.map { it.canchaId }.toSet()
                    canchaIds.forEach { canchaId ->
                        prDao.marcarComoSincronizados(canchaId)
                    }
                    android.util.Log.d(
                        "SyncWorker",
                        "PRs sincronizados: ${response.actualizados} actualizados, ${response.insertados} insertados"
                    )
                } else {
                    android.util.Log.e("SyncWorker", "Error del servidor al sincronizar PRs: ${response.mensaje}")
                    return Result.retry()
                }
            } else {
                android.util.Log.d("SyncWorker", "No hay PRs pendientes de sincronizar")
            }

            // ---------------------------------------------------------------
            // PARTE 2: Sincronizar eliminaciones pendientes
            // ---------------------------------------------------------------
            android.util.Log.d("SyncWorker", "Sincronizando eliminaciones pendientes")

            val eliminacionesPendientes = eliminacionPendienteDao.obtenerTodas()

            if (eliminacionesPendientes.isNotEmpty()) {
                android.util.Log.d("SyncWorker", "Eliminaciones pendientes: ${eliminacionesPendientes.size}")

                eliminacionesPendientes.forEach { eliminacion ->
                    try {
                        when (eliminacion.tipo) {
                            "FUNDO" -> {
                                val response = apiService.eliminarFundo(
                                    EliminarFundoRequest(eliminacion.referenciaId)
                                )
                                if (response.exito) {
                                    eliminacionPendienteDao.eliminar(eliminacion.id)
                                    android.util.Log.d("SyncWorker", "Fundo eliminado en servidor: ${eliminacion.referenciaId}")
                                } else {
                                    android.util.Log.e("SyncWorker", "Error eliminando fundo: ${response.mensaje}")
                                }
                            }
                            "CANCHA" -> {
                                val response = apiService.eliminarCancha(
                                    EliminarCanchaRequest(eliminacion.referenciaId.toInt())
                                )
                                if (response.exito) {
                                    eliminacionPendienteDao.eliminar(eliminacion.id)
                                    android.util.Log.d("SyncWorker", "Cancha eliminada en servidor: ${eliminacion.referenciaId}")
                                } else {
                                    android.util.Log.e("SyncWorker", "Error eliminando cancha: ${response.mensaje}")
                                }
                            }
                            "PR" -> {
                                val response = apiService.eliminarPR(
                                    EliminarPRRequest(eliminacion.referenciaId.toInt())
                                )
                                if (response.exito) {
                                    eliminacionPendienteDao.eliminar(eliminacion.id)
                                    android.util.Log.d("SyncWorker", "PR eliminado en servidor: ${eliminacion.referenciaId}")
                                } else {
                                    android.util.Log.e("SyncWorker", "Error eliminando PR: ${response.mensaje}")
                                }
                            }
                            else -> {
                                android.util.Log.w("SyncWorker", "Tipo de eliminación desconocido: ${eliminacion.tipo}")
                                eliminacionPendienteDao.eliminar(eliminacion.id)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SyncWorker", "Error sincronizando eliminación ${eliminacion.tipo}: ${e.message}")
                    }
                }
            } else {
                android.util.Log.d("SyncWorker", "No hay eliminaciones pendientes")
            }

            android.util.Log.d("SyncWorker", "Sincronización completada exitosamente")
            Result.success()

        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error general en sync: ${e.message}", e)
            Result.retry()
        }
    }
}