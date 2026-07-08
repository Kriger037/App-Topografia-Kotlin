package com.felipe.topografiaapp.domain.usecase

import com.felipe.topografiaapp.data.source.CoordConverter
import com.felipe.topografiaapp.domain.model.CoordenadaResult
import com.felipe.topografiaapp.domain.model.FormatoArchivoCoord
import javax.inject.Inject

// ---------------------------------------------------------------------------
// ConvertirCoordenadasUseCase
//
// Encapsula TODA la lógica de conversión geodésica del proyecto.
// Reemplaza el rol que tenía procesar_upload.php (proj4php) pero ejecutando
// 100% localmente en el dispositivo Android mediante la librería proj4j.
//
// Zonas UTM Chile:
//   Zona 18S → centro y norte del país (Este aprox. 200.000 – 699.999)
//   Zona 19S → sur, cordillera y extremos (Este aprox. 700.000 – 899.999)
// ---------------------------------------------------------------------------

class ConvertirCoordenadasUseCase @Inject constructor(
    private val converter: CoordConverter
) {

    // Convierte una coordenada UTM a Latitud/Longitud
    // zona = 18 o 19 (huso UTM Chile)
    fun utmALatLng(norte: Double, este: Double, zona: Int): CoordenadaResult {
        if (norte <= 0 || este <= 0) {
            return CoordenadaResult.Error("Coordenadas UTM inválidas: norte=$norte, este=$este")
        }
        return converter.utmALatLng(norte, este, zona)
    }

    // Convierte Latitud/Longitud a coordenada UTM
    fun latLngAUtm(latitud: Double, longitud: Double): CoordenadaResult {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            return CoordenadaResult.Error("Lat/Long fuera de rango: lat=$latitud, lng=$longitud")
        }
        return converter.latLngAUtm(latitud, longitud)
    }

    // Detecta automáticamente la zona UTM según el valor del campo Este
    // Heurística basada en rangos de coordenadas UTM para Chile
    fun detectarZonaUtm(este: Double): Int {
        return if (este >= 700_000.0) 19 else 18
    }

    // Detecta el formato de un valor de coordenada de un archivo importado
    // Útil para el ImportUseCase y para mostrar feedback al usuario
    fun detectarFormato(valorNorte: Double, valorEste: Double): FormatoArchivoCoord {
        return when {
            valorNorte > 1_000_000.0 && valorEste in 100_000.0..999_999.0 -> {
                if (valorEste >= 700_000.0) FormatoArchivoCoord.UTM_ZONA_19
                else FormatoArchivoCoord.UTM_ZONA_18
            }
            valorNorte in -90.0..90.0 && valorEste in -180.0..180.0 -> {
                FormatoArchivoCoord.LAT_LNG
            }
            else -> FormatoArchivoCoord.DESCONOCIDO
        }
    }
}
