package com.felipe.topografiaapp

data class LoginResponse(
    val exito: Boolean,
    val mensaje: String,
    val nombre_usuario: String?,
    val rol: String?
)