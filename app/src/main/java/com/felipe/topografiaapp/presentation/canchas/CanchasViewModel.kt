package com.felipe.topografiaapp.presentation.canchas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipe.topografiaapp.domain.model.Cancha
import com.felipe.topografiaapp.domain.usecase.ObtenerCanchasUseCase
import com.felipe.topografiaapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CanchasViewModel @Inject constructor(
    private val obtenerCanchasUseCase: ObtenerCanchasUseCase,
    private val prDao: com.felipe.topografiaapp.data.local.dao.PRDao
) : ViewModel() {

    private val _canchasState = MutableStateFlow<UiState<List<Cancha>>>(UiState.Loading)
    val canchasState: StateFlow<UiState<List<Cancha>>> = _canchasState

    private val _estaOffline = MutableStateFlow(false)
    val estaOffline: StateFlow<Boolean> = _estaOffline

    fun cargarCanchas(codigoFundo: String) {
        viewModelScope.launch {
            _canchasState.value = UiState.Loading
            obtenerCanchasUseCase(codigoFundo)
                .catch { e -> _canchasState.value = UiState.Error(e.message ?: "Error") }
                .collect { lista ->
                    _canchasState.value = if (lista.isEmpty()) UiState.Error("Sin canchas")
                                        else UiState.Success(lista)
                }
        }
        viewModelScope.launch {
            try {
                obtenerCanchasUseCase.sincronizar(codigoFundo)
                _estaOffline.value = false
            } catch (e: Exception){
                _estaOffline.value = true
            }
        }
    }

    fun guardarPRsLocalmente(entities: List<com.felipe.topografiaapp.data.local.entity.PREntity>, canchaId: Int) {
        viewModelScope.launch {
            try {
                prDao.insertarPRs(entities)
            } catch (e: Exception) {
                android.util.Log.e("CanchasViewModel", "Error guardando PRs: ${e.message}")
            }
        }
    }
}