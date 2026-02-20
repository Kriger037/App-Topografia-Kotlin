package com.felipe.topografiaapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvFundos = findViewById<RecyclerView>(R.id.rvFundos)
        rvFundos.layoutManager = LinearLayoutManager(this)

        // 1. Configurar Retrofit
        val retrofit = Retrofit.Builder()
            // IMPORTANTE: Al usar emulador la ip es 10.0.2.2, de lo contrario utilizar la IP del dispositivo
            .baseUrl("http://10.0.2.2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // 2. Crear la instancia del servicio
        val servicio = retrofit.create(ApiService::class.java)

        // 3. Realizar la llamada a la API para obtener usuarios
        val llamada = servicio.obtenerUsuarios()
        llamada.enqueue(object : Callback<List<Usuario>>{
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>){
                if (response.isSuccessful){
                    val usuarios = response.body()
                    Log.d("API_TEST", "Conexión exitosa Usuarios encontrados: ${usuarios?.size}")
                    usuarios?.forEach { usuario ->
                        Log.d("API_TEST", "Usuario: ${usuario.nombre} - ${usuario.rol}")
                    }
                }else{
                    Log.e("API_TEST", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable){
                Log.e("API_TEST", "Falló la conexión: ${t.message}")
            }
        })

        // 4. Realizar la llamada a la API para obtener fundos
        val llamada2 = servicio.obtenerFundos()

        llamada2.enqueue(object : Callback<List<Fundo>>{
            override fun onResponse(call: Call<List<Fundo>>, response: Response<List<Fundo>>){
                if (response.isSuccessful){
                    val fundos = response.body() ?: emptyList()

                    Log.d("API_FUNDOS", "Conexión exitosa Fundos encontrados: ${fundos.size}")
                    val adaptador = FundoAdapter(fundos)
                    rvFundos.adapter = adaptador
            } else{
                Log.e("API_FUNDOS", "Error en la respuesta: ${response.code()}")
                }
                }
            override fun onFailure(call: Call<List<Fundo>>, t: Throwable){
                Log.e("API_FUNDOS", "Falló de red: ${t.message}")
            }
        })
    }
}