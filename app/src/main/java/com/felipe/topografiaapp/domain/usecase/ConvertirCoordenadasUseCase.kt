package com.felipe.topografiaapp.domain.usecase

import com.felipe.topografiaapp.data.source.CoordConverter
import com.felipe.topografiaapp.domain.model.CoordenadaResult
import com.felipe.topografiaapp.domain.model.FormatoArchivoCoord
import javax.inject.Inject

class ConvertirCoordenadasUseCase @Inject constructor(
    private val converter: CoordConverter
) {

    suspend fun utmALatLng(norte: Double, este: Double, zona: Int): CoordenadaResult {
        if (norte <= 0 || este <= 0) {
            return CoordenadaResult.Error("Coordenadas UTM inválidas: norte=$norte, este=$este")
        }
        return converter.utmALatLng(norte, este, zona)
    }

    fun latLngAUtm(latitud: Double, longitud: Double): CoordenadaResult {
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            return CoordenadaResult.Error("Lat/Long fuera de rango: lat=$latitud, lng=$longitud")
        }
        return converter.latLngAUtm(latitud, longitud)
    }

    fun detectarZonaUtm(este: Double): Int {
        return if (este >= 700_000.0) 19 else 18
    }

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