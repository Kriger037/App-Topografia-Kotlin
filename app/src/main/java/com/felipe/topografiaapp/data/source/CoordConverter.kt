package com.felipe.topografiaapp.data.source

import com.felipe.topografiaapp.data.remote.ApiService
import com.felipe.topografiaapp.domain.model.CoordenadaResult
import com.felipe.topografiaapp.util.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class CoordConverter @Inject constructor(
    private val apiService: ApiService,
    private val networkUtils: NetworkUtils
) {

    // -----------------------------------------------------------------------
    // Conversión principal — usa servidor si hay conexión, matemática si no
    // -----------------------------------------------------------------------
    suspend fun utmALatLng(norte: Double, este: Double, zona: Int): CoordenadaResult {
        return if (networkUtils.hayConexion()) {
            utmALatLngServidor(norte, este, zona)
        } else {
            utmALatLngLocal(norte, este, zona)
        }
    }

    // -----------------------------------------------------------------------
    // Opción 1 — Delegada al servidor PHP con PROJ4PHP
    // -----------------------------------------------------------------------
    private suspend fun utmALatLngServidor(norte: Double, este: Double, zona: Int): CoordenadaResult {
        return try {
            android.util.Log.d("CoordConverter", "Convirtiendo por servidor: norte=$norte este=$este zona=$zona")
            val response = apiService.convertirCoordenadas(norte, este, zona)
            if (response.exito && response.latitud != null && response.longitud != null) {
                android.util.Log.d("CoordConverter", "Servidor respondió: lat=${response.latitud} lng=${response.longitud}")
                CoordenadaResult.Exito(
                    latitud = response.latitud,
                    longitud = response.longitud,
                    norte = norte,
                    este = este,
                    zona = zona
                )
            } else {
                android.util.Log.w("CoordConverter", "Servidor falló, usando fórmula local")
                utmALatLngLocal(norte, este, zona)
            }
        } catch (e: Exception) {
            android.util.Log.w("CoordConverter", "Error de red, usando fórmula local: ${e.message}")
            utmALatLngLocal(norte, este, zona)
        }
    }

    // -----------------------------------------------------------------------
    // Opción 2 — Fórmula matemática directa UTM → WGS84
    // Basada en el algoritmo de Bowring, misma precisión que PROJ4PHP
    // -----------------------------------------------------------------------
    private fun utmALatLngLocal(norte: Double, este: Double, zona: Int): CoordenadaResult {
        return try {
            android.util.Log.d("CoordConverter", "Convirtiendo localmente: norte=$norte este=$este zona=$zona")

            // Constantes WGS84
            val a = 6378137.0              // semieje mayor
            val f = 1.0 / 298.257223563   // achatamiento
            val b = a * (1 - f)           // semieje menor
            val e2 = 2 * f - f * f        // excentricidad al cuadrado
            val e4 = e2 * e2
            val e6 = e2 * e2 * e2

            // Parámetros UTM
            val k0 = 0.9996               // factor de escala
            val E0 = 500000.0             // falso este
            val N0 = 10000000.0           // falso norte (hemisferio sur)

            // Meridiano central de la zona
            val lon0 = Math.toRadians(((zona - 1) * 6 - 180 + 3).toDouble())

            val eP2 = e2 / (1 - e2)      // segunda excentricidad al cuadrado

            val M0 = 0.0

            val x = este - E0
            val y = norte - N0            // corrección hemisferio sur

            val M = M0 + y / k0

            val mu = M / (a * (1 - e2 / 4 - 3 * e4 / 64 - 5 * e6 / 256))

            val e1 = (1 - sqrt(1 - e2)) / (1 + sqrt(1 - e2))
            val e1_2 = e1 * e1
            val e1_3 = e1 * e1 * e1
            val e1_4 = e1 * e1 * e1 * e1

            val phi1 = mu +
                    (3 * e1 / 2 - 27 * e1_3 / 32) * sin(2 * mu) +
                    (21 * e1_2 / 16 - 55 * e1_4 / 32) * sin(4 * mu) +
                    (151 * e1_3 / 96) * sin(6 * mu) +
                    (1097 * e1_4 / 512) * sin(8 * mu)

            val N1 = a / sqrt(1 - e2 * sin(phi1).pow(2))
            val T1 = tan(phi1).pow(2)
            val C1 = eP2 * cos(phi1).pow(2)
            val R1 = a * (1 - e2) / (1 - e2 * sin(phi1).pow(2)).pow(1.5)
            val D = x / (N1 * k0)

            val D2 = D * D
            val D3 = D * D * D
            val D4 = D * D * D * D
            val D5 = D * D * D * D * D
            val D6 = D * D * D * D * D * D

            val T1_2 = T1 * T1
            val C1_2 = C1 * C1

            val latRad = phi1 - (N1 * tan(phi1) / R1) * (
                    D2 / 2 -
                            (5 + 3 * T1 + 10 * C1 - 4 * C1_2 - 9 * eP2) * D4 / 24 +
                            (61 + 90 * T1 + 298 * C1 + 45 * T1_2 - 252 * eP2 - 3 * C1_2) * D6 / 720
                    )

            val lonRad = lon0 + (
                    D -
                            (1 + 2 * T1 + C1) * D3 / 6 +
                            (5 - 2 * C1 + 28 * T1 - 3 * C1_2 + 8 * eP2 + 24 * T1_2) * D5 / 120
                    ) / cos(phi1)

            val latitud = Math.toDegrees(latRad)
            val longitud = Math.toDegrees(lonRad)

            android.util.Log.d("CoordConverter", "Resultado local: lat=$latitud lng=$longitud")

            CoordenadaResult.Exito(
                latitud = latitud,
                longitud = longitud,
                norte = norte,
                este = este,
                zona = zona
            )
        } catch (e: Exception) {
            android.util.Log.e("CoordConverter", "Error en fórmula local: ${e.message}", e)
            CoordenadaResult.Error("Error en conversión local: ${e.message}")
        }
    }

    // -----------------------------------------------------------------------
    // Conversión inversa LatLng → UTM (solo fórmula local por ahora)
    // -----------------------------------------------------------------------
    fun latLngAUtm(latitud: Double, longitud: Double): CoordenadaResult {
        return try {
            val a = 6378137.0
            val f = 1.0 / 298.257223563
            val b = a * (1 - f)
            val e2 = 2 * f - f * f
            val k0 = 0.9996
            val E0 = 500000.0
            val N0 = 10000000.0

            val zona = if (longitud < -69.0) 18 else 19
            val lon0 = Math.toRadians(((zona - 1) * 6 - 180 + 3).toDouble())
            android.util.Log.d("CoordConverter", "Zona=$zona meridiano central=${Math.toDegrees(lon0)} grados")

            val latRad = Math.toRadians(latitud)
            val lonRad = Math.toRadians(longitud)

            val N = a / sqrt(1 - e2 * sin(latRad).pow(2))
            val T = tan(latRad).pow(2)
            val C = (e2 / (1 - e2)) * cos(latRad).pow(2)
            val A = cos(latRad) * (lonRad - lon0)

            val e4 = e2 * e2
            val e6 = e2 * e2 * e2

            val M = a * (
                    (1 - e2 / 4 - 3 * e4 / 64 - 5 * e6 / 256) * latRad -
                            (3 * e2 / 8 + 3 * e4 / 32 + 45 * e6 / 1024) * sin(2 * latRad) +
                            (15 * e4 / 256 + 45 * e6 / 1024) * sin(4 * latRad) -
                            (35 * e6 / 3072) * sin(6 * latRad)
                    )

            val A2 = A * A
            val A3 = A * A * A
            val A4 = A * A * A * A
            val A5 = A * A * A * A * A
            val A6 = A * A * A * A * A * A
            val T2 = T * T
            val C2 = C * C

            val este = E0 + k0 * N * (
                    A +
                            (1 - T + C) * A3 / 6 +
                            (5 - 18 * T + T2 + 72 * C - 58 * (e2 / (1 - e2))) * A5 / 120
                    )

            val norte = N0 + k0 * (M + N * tan(latRad) * (
                    A2 / 2 +
                            (5 - T + 9 * C + 4 * C2) * A4 / 24 +
                            (61 - 58 * T + T2 + 600 * C - 330 * (e2 / (1 - e2))) * A6 / 720
                    ))

            CoordenadaResult.Exito(
                latitud = latitud,
                longitud = longitud,
                norte = norte,
                este = este,
                zona = zona
            )
        } catch (e: Exception) {
            CoordenadaResult.Error("Error en conversión LatLng→UTM: ${e.message}")
        }
    }
}