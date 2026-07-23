package com.felipe.topografiaapp.presentation.prs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipe.topografiaapp.domain.model.PR
import com.felipe.topografiaapp.domain.usecase.ConvertirCoordenadasUseCase
import com.felipe.topografiaapp.domain.usecase.ObtenerPRsUseCase
import com.felipe.topografiaapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PRsViewModel @Inject constructor(
    private val obtenerPRsUseCase: ObtenerPRsUseCase,
    private val convertirCoordenadasUseCase: ConvertirCoordenadasUseCase
) : ViewModel() {

    private val _prsState = MutableStateFlow<UiState<List<PR>>>(UiState.Loading)
    val prsState: StateFlow<UiState<List<PR>>> = _prsState

    private val _estaOffline = MutableStateFlow(false)
    val estaOffline: StateFlow<Boolean> = _estaOffline

    fun cargarPRs(canchaId: Int) {
        viewModelScope.launch {
            _prsState.value = UiState.Loading
            android.util.Log.d("PRsViewModel", "Iniciando carga para canchaId=$canchaId")
            try {
                obtenerPRsUseCase.sincronizar(canchaId)
                _estaOffline.value = false
                android.util.Log.d("PRsViewModel", "Sincronización completada")
            } catch (e: Exception) {
                _estaOffline.value = true
                android.util.Log.e("PRsViewModel", "Error sincronizando: ${e.message}")
            }

            obtenerPRsUseCase(canchaId)
                .catch { e ->
                    android.util.Log.e("PRsViewModel", "Error en Flow: ${e.message}")
                    _prsState.value = UiState.Error(e.message ?: "Error")
                }
                .collect { lista ->
                    android.util.Log.d("PRsViewModel", "Flow emitió lista con ${lista.size} elementos")
                    _prsState.value = if (lista.isEmpty()) {
                        UiState.Error("Esta cancha no tiene PRs registrados")
                    } else {
                        UiState.Success(lista)
                    }
                }
        }
    }
}