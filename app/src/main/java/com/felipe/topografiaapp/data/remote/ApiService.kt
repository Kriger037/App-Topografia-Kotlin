package com.felipe.topografiaapp.data.remote

import com.felipe.topografiaapp.data.remote.dto.CanchaDto
import com.felipe.topografiaapp.data.remote.dto.FundoDto
import com.felipe.topografiaapp.data.remote.dto.PRDto
import com.felipe.topografiaapp.LoginResponse
import com.felipe.topografiaapp.data.remote.dto.ActualizarPRsRequest
import com.felipe.topografiaapp.data.remote.dto.ActualizarPRsResponse
import com.felipe.topografiaapp.data.remote.dto.ConversionResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("topografia/obtener_fundos.php")
    suspend fun obtenerFundos(): List<FundoDto>

    @GET("topografia/obtener_canchas.php")
    suspend fun obtenerCanchas(@Query("codigo_fundo") codigoFundo: String): List<CanchaDto>

    @GET("topografia/obtener_prs.php")
    suspend fun obtenerPRs(@Query("cancha_id") canchaId: Int): List<PRDto>

    @FormUrlEncoded
    @POST("topografia/login.php")
    fun iniciarSesion(
        @Field("usuario") usuario: String,
        @Field("contrasena") contrasena: String
    ): Call<LoginResponse>

    @GET("topografia/convertir_coordenadas.php")
    suspend fun convertirCoordenadas(
        @Query("norte") norte: Double,
        @Query("este") este: Double,
        @Query("zona") zona: Int
    ): ConversionResponse

    @POST("topografia/actualizar_prs.php")
    suspend fun actualizarPRs(@Body body: ActualizarPRsRequest): ActualizarPRsResponse
}