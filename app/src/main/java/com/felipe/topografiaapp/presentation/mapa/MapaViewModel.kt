package com.felipe.topografiaapp.presentation.mapa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipe.topografiaapp.domain.model.PR
import com.felipe.topografiaapp.domain.usecase.ObtenerPRsUseCase
import com.felipe.topografiaapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapaViewModel @Inject constructor(
    private val obtenerPRsUseCase: ObtenerPRsUseCase
) : ViewModel() {

    private val _prsState = MutableStateFlow<UiState<List<PR>>>(UiState.Loading)
    val prsState: StateFlow<UiState<List<PR>>> = _prsState

    fun cargarPRsParaMapa(canchaId: Int) {
        viewModelScope.launch {
            _prsState.value = UiState.Loading
            obtenerPRsUseCase(canchaId)
                .catch { e -> _prsState.value = UiState.Error(e.message ?: "Error") }
                .collect { lista ->
                    _prsState.value = if (lista.isEmpty()) UiState.Error("Sin PRs")
                    else UiState.Success(lista)
                }
        }
    }
}