package com.felipe.topografiaapp.data.source

import com.felipe.topografiaapp.domain.model.CoordenadaResult
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// CoordConverter
//
// Usa cadenas de proyección PROJ4 directas en vez de códigos EPSG.
// Esto evita la dependencia de los archivos proj4/nad/epsg que no están
// disponibles en Android, causando IllegalStateException en runtime.
//
// Proyecciones equivalentes:
//   EPSG:32718 → UTM Zona 18 Sur → cadena proj4 directa
//   EPSG:32719 → UTM Zona 19 Sur → cadena proj4 directa
//   EPSG:4326  → WGS84 LatLng    → cadena proj4 directa
// ---------------------------------------------------------------------------

@Singleton
class CoordConverter @Inject constructor() {

    private val crsFactory = CRSFactory()
    private val ctFactory = CoordinateTransformFactory()

    // Definiciones PROJ4 directas sin depender de archivos EPSG externos
    private val wgs84 by lazy {
        crsFactory.createFromParameters(
            "WGS84",
            "+proj=longlat +datum=WGS84 +no_defs"
        )
    }

    private val utm18Sur by lazy {
        crsFactory.createFromParameters(
            "UTM18S",
            "+proj=utm +zone=18 +south +datum=WGS84 +units=m +no_defs"
        )
    }

    private val utm19Sur by lazy {
        crsFactory.createFromParameters(
            "UTM19S",
            "+proj=utm +zone=19 +south +datum=WGS84 +units=m +no_defs"
        )
    }

    private val transformUtm18aWgs84: CoordinateTransform by lazy {
        ctFactory.createTransform(utm18Sur, wgs84)
    }

    private val transformUtm19aWgs84: CoordinateTransform by lazy {
        ctFactory.createTransform(utm19Sur, wgs84)
    }

    private val transformWgs84aUtm18: CoordinateTransform by lazy {
        ctFactory.createTransform(wgs84, utm18Sur)
    }

    private val transformWgs84aUtm19: CoordinateTransform by lazy {
        ctFactory.createTransform(wgs84, utm19Sur)
    }

    fun utmALatLng(norte: Double, este: Double, zona: Int): CoordenadaResult {
        return try {
            android.util.Log.d("CoordConverter", "Convirtiendo: norte=$norte este=$este zona=$zona")

            val puntoBruto = ProjCoordinate(este, norte)
            val puntoResultado = ProjCoordinate()

            val transformador = when (zona) {
                18 -> transformUtm18aWgs84
                19 -> transformUtm19aWgs84
                else -> return CoordenadaResult.Error("Zona UTM no soportada: $zona. Usar 18 o 19.")
            }

            synchronized(transformador) {
                transformador.transform(puntoBruto, puntoResultado)
            }

            android.util.Log.d("CoordConverter", "Resultado: lat=${puntoResultado.y} lng=${puntoResultado.x}")

            CoordenadaResult.Exito(
                latitud = puntoResultado.y,
                longitud = puntoResultado.x,
                norte = norte,
                este = este,
                zona = zona
            )
        } catch (e: Exception) {
            android.util.Log.e("CoordConverter", "Error en conversión: ${e.message}", e)
            CoordenadaResult.Error("Error en conversión UTM→LatLng: ${e.message}")
        }
    }

    fun latLngAUtm(latitud: Double, longitud: Double): CoordenadaResult {
        return try {
            val zona = if (longitud < -69.0) 18 else 19

            val puntoWgs84 = ProjCoordinate(longitud, latitud)
            val puntoUtm = ProjCoordinate()

            val transformador = when (zona) {
                18 -> transformWgs84aUtm18
                19 -> transformWgs84aUtm19
                else -> return CoordenadaResult.Error("No se pudo determinar la zona UTM")
            }

            synchronized(transformador) {
                transformador.transform(puntoWgs84, puntoUtm)
            }

            CoordenadaResult.Exito(
                latitud = latitud,
                longitud = longitud,
                norte = puntoUtm.y,
                este = puntoUtm.x,
                zona = zona
            )
        } catch (e: Exception) {
            CoordenadaResult.Error("Error en conversión LatLng→UTM: ${e.message}")
        }
    }

    fun convertirListaUtmALatLng(
        puntos: List<Triple<Double, Double, Double>>,
        zona: Int
    ): List<CoordenadaResult> {
        return puntos.map { (norte, este, _) ->
            utmALatLng(norte, este, zona)
        }
    }
}