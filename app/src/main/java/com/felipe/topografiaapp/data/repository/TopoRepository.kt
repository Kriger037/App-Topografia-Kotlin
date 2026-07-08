package com.felipe.topografiaapp.data.repository

import com.felipe.topografiaapp.data.local.dao.CanchaDao
import com.felipe.topografiaapp.data.local.dao.FundoDao
import com.felipe.topografiaapp.data.local.dao.PRDao
import com.felipe.topografiaapp.data.remote.ApiService
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
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TopoRepository @Inject constructor(
    private val fundoDao: FundoDao,
    private val canchaDao: CanchaDao,
    private val prDao: PRDao,
    private val apiService: ApiService,
    private val coordConverter: CoordConverter
) : ITopoRepository {

    // -----------------------------------------------------------------------
    // FUNDOS
    // -----------------------------------------------------------------------

    override fun obtenerFundos(): Flow<List<Fundo>> =
        fundoDao.obtenerTodosFlow().map { entities -> entities.map { it.toDomain() } }

    override suspend fun sincronizarFundos() {
        try {
            val dtos = apiService.obtenerFundos()
            fundoDao.insertarFundos(dtos.map { it.toEntity() })
        } catch (e: Exception) {
            // Si falla la red, Room ya tiene los datos locales — no es error crítico
        }
    }

    // -----------------------------------------------------------------------
    // CANCHAS
    // -----------------------------------------------------------------------

    override fun obtenerCanchasPorFundo(codigoFundo: String): Flow<List<Cancha>> =
        canchaDao.obtenerPorFundoFlow(codigoFundo).map { entities -> entities.map { it.toDomain() } }

    override suspend fun sincronizarCanchasPorFundo(codigoFundo: String) {
        try {
            val dtos = apiService.obtenerCanchas(codigoFundo)
            canchaDao.insertarCanchas(dtos.map { it.toEntity() })
        } catch (e: Exception) {

        }
    }

     override fun obtenerPRsPorCancha(canchaId: Int): Flow<List<PR>> =
        prDao.obtenerPorCanchaFlow(canchaId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun sincronizarPRsPorCancha(canchaId: Int) {
        try {
            android.util.Log.d("TopoRepo", "Intentando sincronizar PRs para canchaId=$canchaId")
            val dtos = apiService.obtenerPRs(canchaId)
            android.util.Log.d("TopoRepo", "PRs recibidos del servidor: ${dtos.size}")
            val huso = canchaDao.obtenerHuso(canchaId) ?: 18
            val entities = dtos.map { dto ->
                val entity = dto.toEntity(canchaId)
                if (entity.latitud == null && entity.longitud == null
                    && entity.norte > 0 && entity.este > 0
                ) {
                    val conversion = coordConverter.utmALatLng(entity.norte, entity.este, huso)
                    if (conversion is com.felipe.topografiaapp.domain.model.CoordenadaResult.Exito) {
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

    // -----------------------------------------------------------------------
    // Importación local (archivos .txt/.csv desde el dispositivo)
    // Los PRs se marcan con isDirty = true para sincronizarlos luego
    // -----------------------------------------------------------------------

    override suspend fun guardarPRsLocalmente(prs: List<PR>, canchaId: Int) {
        val entities = prs.map { pr ->
            // Si el PR importado no tiene coordenadas geográficas, convertir ahora
            val prConCoords = if (pr.latitud == null && pr.norte > 0 && pr.este > 0) {
                val huso = canchaDao.obtenerHuso(canchaId) ?: 18
                val conversion = coordConverter.utmALatLng(pr.norte, pr.este, huso)
                if (conversion is CoordenadaResult.Exito) {
                    pr.copy(
                        latitud = conversion.latitud,
                        longitud = conversion.longitud,
                        isDirty = true
                    )
                } else pr.copy(isDirty = true)
            } else pr.copy(isDirty = true)

            prConCoords.toEntity()
        }
        prDao.insertarPRs(entities)
    }

    override suspend fun obtenerPRsPendientesSincronizacion(): List<PR> =
        prDao.obtenerPendientesSincronizacion().map { it.toDomain() }

    override suspend fun marcarPRsComoSincronizados(canchaId: Int) =
        prDao.marcarComoSincronizados(canchaId)
}
