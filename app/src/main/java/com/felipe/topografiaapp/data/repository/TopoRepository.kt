package com.felipe.topografiaapp.data.repository

import com.felipe.topografiaapp.data.local.dao.CanchaDao
import com.felipe.topografiaapp.data.local.dao.EliminacionPendienteDao
import com.felipe.topografiaapp.data.local.dao.FundoDao
import com.felipe.topografiaapp.data.local.dao.PRDao
import com.felipe.topografiaapp.data.local.entity.EliminacionPendienteEntity
import com.felipe.topografiaapp.data.remote.ApiService
import com.felipe.topografiaapp.data.remote.dto.EliminarCanchaRequest
import com.felipe.topografiaapp.data.remote.dto.EliminarFundoRequest
import com.felipe.topografiaapp.data.remote.dto.EliminarPRRequest
import com.felipe.topografiaapp.data.source.CoordConverter
import com.felipe.topografiaapp.domain.model.Cancha
import com.felipe.topografiaapp.domain.model.CoordenadaResult
import com.felipe.topografiaapp.domain.model.Fundo
import com.felipe.topografiaapp.domain.model.PR
import com.felipe.topografiaapp.domain.repository.ITopoRepository
import com.felipe.topografiaapp.util.toDomain
import com.felipe.topografiaapp.util.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopoRepository @Inject constructor(
    private val fundoDao: FundoDao,
    private val canchaDao: CanchaDao,
    private val prDao: PRDao,
    private val eliminacionPendienteDao: EliminacionPendienteDao,
    private val apiService: ApiService,
    private val coordConverter: CoordConverter
) : ITopoRepository {

    override fun obtenerFundos(): Flow<List<Fundo>> =
        fundoDao.obtenerTodosFlow().map { entities -> entities.map { it.toDomain() } }

    override suspend fun sincronizarFundos() {
        try {
            val dtos = apiService.obtenerFundos()
            fundoDao.insertarFundos(dtos.map { it.toEntity() })
        } catch (e: Exception) {}
    }

    override fun obtenerCanchasPorFundo(codigoFundo: String): Flow<List<Cancha>> =
        canchaDao.obtenerPorFundoFlow(codigoFundo).map { entities -> entities.map { it.toDomain() } }

    override suspend fun sincronizarCanchasPorFundo(codigoFundo: String) {
        try {
            val dtos = apiService.obtenerCanchas(codigoFundo)
            canchaDao.insertarCanchas(dtos.map { it.toEntity() })
        } catch (e: Exception) {}
    }

    override fun obtenerPRsPorCancha(canchaId: Int): Flow<List<PR>> =
        prDao.obtenerPorCanchaFlow(canchaId).map { entities ->
            entities.forEach { entity ->
                android.util.Log.d("TopoRepo", "PR: ${entity.descriptor} lat=${entity.latitud} lng=${entity.longitud} isDirty=${entity.isDirty}")
            }
            entities.map { it.toDomain() }
        }

    override suspend fun sincronizarPRsPorCancha(canchaId: Int) {
        try {
            android.util.Log.d("TopoRepo", "Intentando sincronizar PRs para canchaId=$canchaId")
            val dtos = apiService.obtenerPRs(canchaId)
            android.util.Log.d("TopoRepo", "PRs recibidos del servidor: ${dtos.size}")

            val huso = canchaDao.obtenerPorId(canchaId)?.huso?.toIntOrNull() ?: 18

            val descriptoresDirty = prDao.obtenerPendientesSincronizacion()
                .filter { it.canchaId == canchaId }
                .map { it.descriptor }
                .toSet()

            android.util.Log.d("TopoRepo", "PRs locales protegidos (isDirty): ${descriptoresDirty.size}")

            val entities = dtos.mapNotNull { dto ->
                if (dto.descriptor in descriptoresDirty) {
                    android.util.Log.d("TopoRepo", "Protegiendo PR local: ${dto.descriptor}")
                    return@mapNotNull null
                }

                val entity = dto.toEntity(canchaId)
                if (entity.latitud == null && entity.longitud == null
                    && entity.norte > 0 && entity.este > 0
                ) {
                    val conversion = coordConverter.utmALatLng(entity.norte, entity.este, huso)
                    if (conversion is CoordenadaResult.Exito) {
                        entity.copy(latitud = conversion.latitud, longitud = conversion.longitud)
                    } else entity
                } else entity
            }

            prDao.insertarPRs(entities)
            android.util.Log.d("TopoRepo", "PRs guardados en Room: ${entities.size}")
        } catch (e: Exception) {
            android.util.Log.e("TopoRepo", "Error sincronizando PRs: ${e.message}", e)
        }
    }

    override suspend fun guardarPRsLocalmente(prs: List<PR>, canchaId: Int) {
        val entities = prs.map { pr ->
            val prFinal = if (pr.latitud == null && pr.longitud == null
                && pr.norte > 0 && pr.este > 0
            ) {
                val huso = canchaDao.obtenerPorId(canchaId)?.huso?.toIntOrNull() ?: 18
                val conversion = coordConverter.utmALatLng(pr.norte, pr.este, huso)
                if (conversion is CoordenadaResult.Exito) {
                    pr.copy(
                        latitud = conversion.latitud,
                        longitud = conversion.longitud,
                        isDirty = true
                    )
                } else pr.copy(isDirty = true)
            } else {
                pr.copy(isDirty = true)
            }
            prFinal.toEntity()
        }
        prDao.insertarPRs(entities)
    }

    override suspend fun obtenerPRsPendientesSincronizacion(): List<PR> =
        prDao.obtenerPendientesSincronizacion().map { it.toDomain() }

    override suspend fun marcarPRsComoSincronizados(canchaId: Int) =
        prDao.marcarComoSincronizados(canchaId)

    // -----------------------------------------------------------------------
    // Eliminación local con registro de pendientes
    // -----------------------------------------------------------------------

    private fun fechaActual() =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    override suspend fun eliminarFundoLocalmente(codigoFundo: String) {
        fundoDao.eliminarPorCodigo(codigoFundo)
        eliminacionPendienteDao.insertar(
            EliminacionPendienteEntity(
                tipo = "FUNDO",
                referenciaId = codigoFundo,
                fechaEliminacion = fechaActual()
            )
        )
    }

    override suspend fun eliminarCanchaLocalmente(canchaId: Int) {
        canchaDao.eliminarPorId(canchaId)
        eliminacionPendienteDao.insertar(
            EliminacionPendienteEntity(
                tipo = "CANCHA",
                referenciaId = canchaId.toString(),
                fechaEliminacion = fechaActual()
            )
        )
    }

    override suspend fun eliminarPRLocalmente(prId: Int) {
        prDao.eliminarPorId(prId)
        eliminacionPendienteDao.insertar(
            EliminacionPendienteEntity(
                tipo = "PR",
                referenciaId = prId.toString(),
                fechaEliminacion = fechaActual()
            )
        )
    }

    override suspend fun sincronizarEliminaciones() {
        val pendientes = eliminacionPendienteDao.obtenerTodas()
        pendientes.forEach { eliminacion ->
            try {
                when (eliminacion.tipo) {
                    "FUNDO"  -> apiService.eliminarFundo(EliminarFundoRequest(eliminacion.referenciaId))
                    "CANCHA" -> apiService.eliminarCancha(EliminarCanchaRequest(eliminacion.referenciaId.toInt()))
                    "PR"     -> apiService.eliminarPR(EliminarPRRequest(eliminacion.referenciaId.toInt()))
                }
                eliminacionPendienteDao.eliminar(eliminacion.id)
            } catch (e: Exception) {
                android.util.Log.e("TopoRepo", "Error eliminando ${eliminacion.tipo}: ${e.message}")
            }
        }
    }
}