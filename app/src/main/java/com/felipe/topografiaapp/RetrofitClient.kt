package com.felipe.topografiaapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.felipe.topografiaapp.data.remote.ApiService
object RetrofitClient {

    const val BASE_URL = BuildConfig.BASE_URL

    private val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}