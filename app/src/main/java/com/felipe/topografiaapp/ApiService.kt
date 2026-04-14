package com.felipe.topografiaapp

import android.adservices.adid.AdId
import retrofit2.Call;
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET;
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    //Prueba para obtener usuarios
    @GET("topografia/test_api.php")
    fun obtenerUsuarios(): Call<List<Usuario>>

    //Prueba para obtener fundos
    @GET("topografia/obtener_fundos.php")
    fun obtenerFundos(): Call<List<Fundo>>

    //Obtener canchas por codigo de fundo
    @GET("topografia/obtener_canchas.php")
    fun obtenerCanchas(
        @Query("codigo_fundo") codigo: String
    ): Call<List<Cancha>>

    //Peticion para obtener PRs por id de cancha
    @GET("topografia/obtener_prs.php")
    fun obtenerPRs(
        @Query("cancha_id") canchaId: Int
    ): Call<List<PR>>

    @FormUrlEncoded
    @POST("topografia/login.php")
    fun iniciarSesion(
        @Field("usuario") usuario: String,
        @Field("contrasena") contrasena: String
    ): Call<LoginResponse>
}