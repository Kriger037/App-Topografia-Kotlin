package com.felipe.topografiaapp.domain.usecase

import android.content.Context
import android.net.Uri
import com.felipe.topografiaapp.data.source.CoordConverter
import com.felipe.topografiaapp.domain.model.CoordenadaResult
import com.felipe.topografiaapp.domain.model.FormatoArchivoCoord
import com.felipe.topografiaapp.domain.model.PR
import com.felipe.topografiaapp.domain.model.ResultadoImportacion
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileImportUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coordConverter: CoordConverter
) {

    val carpetaPredeterminada: File
        get() {
            val carpeta = File(
                context.getExternalFilesDir(null),
                "Topografia/Coordenadas"
            )
            if (!carpeta.exists()) carpeta.mkdirs()
            return carpeta
        }

    suspend fun procesarArchivo(uri: Uri, canchaId: Int, huso: Int? = null): ResultadoImportacion {
        val lineas = leerLineas(uri)
        return parsearLineas(lineas, canchaId, huso)
    }

    suspend fun procesarArchivoPredeterminado(nombreArchivo: String, canchaId: Int, huso: Int? = null): ResultadoImportacion {
        val archivo = File(carpetaPredeterminada, nombreArchivo)
        val lineas = archivo.readLines()
        return parsearLineas(lineas, canchaId, huso)
    }

    fun listarArchivosDisponibles(): List<String> {
        return carpetaPredeterminada
            .listFiles { file -> file.extension == "txt" || file.extension == "csv" }
            ?.map { it.name }
            ?: emptyList()
    }

    private suspend fun parsearLineas(lineas: List<String>, canchaId: Int, husoForzado: Int? = null): ResultadoImportacion {
        val puntosValidos = mutableListOf<PR>()
        var lineasIgnoradas = 0
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        for (linea in lineas) {
            val lineaTrimmed = linea.trim()
            if (lineaTrimmed.isEmpty()) continue

            val pedazos = lineaTrimmed.split(Regex("[,\\t;]+|\\s+"))

            var descriptor = ""
            var norte = 0.0
            var este = 0.0
            var cota = 0.0
            val numerosSobrantes = mutableListOf<Double>()

            for (pedazo in pedazos) {
                val valor = pedazo.trim()
                if (valor.isEmpty()) continue

                when {
                    valor.contains(Regex("[a-zA-Z]")) -> {
                        if (valor.contains("PR", ignoreCase = true)) {
                            descriptor = valor.uppercase()
                        }
                    }
                    valor.toDoubleOrNull() != null && valor.toDouble() > 1_000_000.0 -> {
                        norte = valor.toDouble()
                    }
                    valor.toDoubleOrNull() != null
                            && valor.toDouble() >= 100_000.0
                            && valor.toDouble() <= 999_999.0 -> {
                        este = valor.toDouble()
                    }
                    valor.toDoubleOrNull() != null -> {
                        numerosSobrantes.add(valor.toDouble())
                    }
                }
            }

            if (numerosSobrantes.isNotEmpty()) {
                val cotaConDecimal = numerosSobrantes.firstOrNull { it.toString().contains(".") }
                cota = cotaConDecimal ?: numerosSobrantes.last()
            }

            if (descriptor.isNotEmpty() && norte > 0 && este > 0) {
                val conversion = coordConverter.utmALatLng(norte, este, husoForzado ?: 18)

                val latitud = if (conversion is CoordenadaResult.Exito) conversion.latitud else null
                val longitud = if (conversion is CoordenadaResult.Exito) conversion.longitud else null

                puntosValidos.add(
                    PR(
                        id = 0,
                        canchaId = canchaId,
                        descriptor = descriptor,
                        norte = norte,
                        este = este,
                        cota = cota,
                        latitud = latitud,
                        longitud = longitud,
                        fechaCreacion = fechaActual,
                        fechaModificacion = fechaActual,
                        isDirty = true
                    )
                )
            } else {
                lineasIgnoradas++
            }
        }

        val zonaDetectada = husoForzado ?: 18

        val formatoDetectado = when (zonaDetectada) {
            19 -> FormatoArchivoCoord.UTM_ZONA_19
            else -> FormatoArchivoCoord.UTM_ZONA_18
        }

        return ResultadoImportacion(
            puntosValidos = puntosValidos,
            lineasIgnoradas = lineasIgnoradas,
            formatoDetectado = formatoDetectado,
            zonaDetectada = zonaDetectada
        )
    }

    private fun leerLineas(uri: Uri): List<String> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo abrir el archivo")
        return inputStream.bufferedReader().readLines()
    }
}