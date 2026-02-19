package com.felipe.topografiaapp

import retrofit2.Call;
import retrofit2.http.GET;

interface ApiService {
    //Prueba para obtener usuarios
    @GET("topografia/test_api.php")
    fun obtenerUsuarios(): Call<List<Usuario>>

    //Prueba para obtener fundos
    @GET("topografia/obtener_fundos.php")
    fun obtenerFundos(): Call<List<Fundo>>
}