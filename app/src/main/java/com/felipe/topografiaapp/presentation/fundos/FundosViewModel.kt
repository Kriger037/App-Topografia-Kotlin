package com.felipe.topografiaapp.presentation.fundos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipe.topografiaapp.domain.model.Fundo
import com.felipe.topografiaapp.domain.usecase.ObtenerFundosUseCase
import com.felipe.topografiaapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FundosViewModel @Inject constructor(
    private val obtenerFundosUseCase: ObtenerFundosUseCase
) : ViewModel() {

    private val _fundosState = MutableStateFlow<UiState<List<Fundo>>>(UiState.Loading)
    val fundosState: StateFlow<UiState<List<Fundo>>> = _fundosState

    private val _estaOffline = MutableStateFlow(false)
    val estaOffline: StateFlow<Boolean> = _estaOffline

    fun cargarFundos() {
        viewModelScope.launch {
            _fundosState.value = UiState.Loading
            obtenerFundosUseCase()
                .catch { e -> _fundosState.value = UiState.Error(e.message ?: "Error") }
                .collect { lista ->
                    _fundosState.value = if (lista.isEmpty()) UiState.Error("Sin fundos")
                    else UiState.Success(lista)
                }
        }
        viewModelScope.launch {
            try {
                obtenerFundosUseCase.sincronizar()
                _estaOffline.value = false
            } catch (e: Exception) {
                _estaOffline.value = true
            }
        }
    }

    fun eliminarFundo(codigoFundo: String) {
        viewModelScope.launch {
            try {
                obtenerFundosUseCase.eliminarLocalmente(codigoFundo)
            } catch (e: Exception) {
                android.util.Log.e("FundosViewModel", "Error eliminando fundo: ${e.message}")
            }
        }
    }
}