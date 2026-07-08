package com.felipe.topografiaapp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.felipe.topografiaapp.data.remote.ApiService
import com.felipe.topografiaapp.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val loginState: StateFlow<UiState<String>> = _loginState

    fun iniciarSesion(usuario: String, contrasena: String){
        viewModelScope.launch {
            try {
                _loginState.value = UiState.Success(usuario)
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Error de autenticación")
            }
        }
    }
}