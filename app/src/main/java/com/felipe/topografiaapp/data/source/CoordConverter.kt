package com.felipe.topografiaapp.data.source

import com.felipe.topografiaapp.domain.model.CoordenadaResult
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.CoordinateTransform
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// CoordConverter
//
// Wrapper de la librería proj4j para conversión de coordenadas geodésicas.
// Equivalente nativo de la librería PROJ4PHP usada en procesar_upload.php.
//
// Dependencia en build.gradle.kts:
//   implementation("org.locationtech.proj4j:proj4j:1.3.0")
//
// Proyecciones utilizadas:
//   EPSG:32718 → UTM Zona 18 Sur (Chile centro/norte)
//   EPSG:32719 → UTM Zona 19 Sur (Chile sur y cordillera)
//   EPSG:4326  → WGS84 (Latitud/Longitud, el formato de Google Maps)
// ---------------------------------------------------------------------------

@Singleton
class CoordConverter @Inject constructor() {

    private val crsFactory = CRSFactory()
    private val ctFactory = CoordinateTransformFactory()

    // Proyección WGS84 (LatLng), compartida entre todas las transformaciones
    private val wgs84: CoordinateReferenceSystem by lazy {
        crsFactory.createFromName("EPSG:4326")
    }

    // Cache de las CRS UTM para evitar recrearlas en cada llamada
    private val crsUtm18: CoordinateReferenceSystem by lazy {
        crsFactory.createFromName("EPSG:32718")
    }
    private val crsUtm19: CoordinateReferenceSystem by lazy {
        crsFactory.createFromName("EPSG:32719")
    }

    // Transformadores cacheados (thread-safe con synchronized)
    private val transformUtm18aWgs84: CoordinateTransform by lazy {
        ctFactory.createTransform(crsUtm18, wgs84)
    }
    private val transformUtm19aWgs84: CoordinateTransform by lazy {
        ctFactory.createTransform(crsUtm19, wgs84)
    }
    private val transformWgs84aUtm18: CoordinateTransform by lazy {
        ctFactory.createTransform(wgs84, crsUtm18)
    }
    private val transformWgs84aUtm19: CoordinateTransform by lazy {
        ctFactory.createTransform(wgs84, crsUtm19)
    }

    // -----------------------------------------------------------------------
    // UTM → LatLng
    // Equivalente a lo que hace procesar_upload.php con proj4php:
    //   $puntoUTM = new Point($este, $norte, $projUTM);
    //   $puntoLatLong = $proj4->transform($projWGS84, $puntoUTM);
    // -----------------------------------------------------------------------
    fun utmALatLng(norte: Double, este: Double, zona: Int): CoordenadaResult {
        return try {
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

            // proj4j devuelve x=longitud, y=latitud en WGS84
            CoordenadaResult.Exito(
                latitud = puntoResultado.y,
                longitud = puntoResultado.x,
                norte = norte,
                este = este,
                zona = zona
            )
        } catch (e: Exception) {
            CoordenadaResult.Error("Error en conversión UTM→LatLng: ${e.message}")
        }
    }

    // -----------------------------------------------------------------------
    // LatLng → UTM
    // Conversión inversa. Detecta automáticamente la zona según la longitud.
    // -----------------------------------------------------------------------
    fun latLngAUtm(latitud: Double, longitud: Double): CoordenadaResult {
        return try {
            // Determinar zona UTM basado en la longitud geográfica
            // Chile continental: Zona 18S (longitud -69° a -75°) / Zona 19S (más al este)
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

            // proj4j devuelve x=Este, y=Norte en UTM
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

    // -----------------------------------------------------------------------
    // Conversión de lista completa (para uso en ImportUseCase)
    // Procesa múltiples puntos en una sola llamada, eficiente para archivos grandes
    // -----------------------------------------------------------------------
    fun convertirListaUtmALatLng(
        puntos: List<Triple<Double, Double, Double>>, // norte, este, cota
        zona: Int
    ): List<CoordenadaResult> {
        return puntos.map { (norte, este, _) ->
            utmALatLng(norte, este, zona)
        }
    }
}
