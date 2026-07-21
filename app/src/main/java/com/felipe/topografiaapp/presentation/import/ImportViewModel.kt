package com.felipe.topografiaapp.presentation.import_coords

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipe.topografiaapp.domain.model.ResultadoImportacion
import com.felipe.topografiaapp.domain.repository.ITopoRepository
import com.felipe.topografiaapp.domain.usecase.FileImportUseCase
import com.felipe.topografiaapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val fileImportUseCase: FileImportUseCase,
    private val repository: ITopoRepository
) : ViewModel() {

    // Estado del proceso de importación
    private val _importState = MutableStateFlow<UiState<ResultadoImportacion>>(UiState.Loading)
    val importState: StateFlow<UiState<ResultadoImportacion>> = _importState

    // Estado del guardado final en Room
    private val _guardadoState = MutableStateFlow<UiState<Int>>(UiState.Loading)
    val guardadoState: StateFlow<UiState<Int>> = _guardadoState

    // Ruta de la carpeta predeterminada para mostrarla al usuario
    val rutaCarpeta: String
        get() = fileImportUseCase.carpetaPredeterminada.absolutePath

    // Lista de archivos disponibles en la carpeta predeterminada
    val archivosDisponibles: List<String>
        get() = fileImportUseCase.listarArchivosDisponibles()

    // Resultado temporal del parsing, guardado para confirmar después
    private var resultadoPendiente: ResultadoImportacion? = null

    // Procesa un archivo elegido con el picker del sistema
    fun procesarArchivoDesdeUri(uri: Uri, canchaId: Int, huso: Int) {
        viewModelScope.launch {
            try {
                _importState.value = UiState.Loading
                val resultado = fileImportUseCase.procesarArchivo(uri, canchaId, huso)
                resultadoPendiente = resultado
                _importState.value = UiState.Success(resultado)
            } catch (e: Exception) {
                _importState.value = UiState.Error("Error al leer el archivo: ${e.message}")
            }
        }
    }

    fun procesarArchivoDesdeNombre(nombre: String, canchaId: Int, huso: Int) {
        viewModelScope.launch {
            try {
                _importState.value = UiState.Loading
                val resultado = fileImportUseCase.procesarArchivoPredeterminado(nombre, canchaId, huso)
                resultadoPendiente = resultado
                _importState.value = UiState.Success(resultado)
            } catch (e: Exception) {
                _importState.value = UiState.Error("Error al leer el archivo: ${e.message}")
            }
        }
    }

    // Confirma el guardado en Room después de que el usuario revisa la vista previa
    fun confirmarImportacion(canchaId: Int) {
        val resultado = resultadoPendiente ?: return
        viewModelScope.launch {
            try {
                repository.guardarPRsLocalmente(resultado.puntosValidos, canchaId)
                _guardadoState.value = UiState.Success(resultado.puntosValidos.size)
                resultadoPendiente = null
            } catch (e: Exception) {
                _guardadoState.value = UiState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    fun limpiarEstado() {
        _importState.value = UiState.Loading
        _guardadoState.value = UiState.Loading
        resultadoPendiente = null
    }
}