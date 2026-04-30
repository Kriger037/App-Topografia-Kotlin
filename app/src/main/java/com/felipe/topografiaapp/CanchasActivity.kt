package com.felipe.topografiaapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CanchasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canchas)

        val codigoFundo = intent.getStringExtra("CODIGO_FUNDO") ?: "Sin Código"
        val nombreFundo = intent.getStringExtra("NOMBRE_FUNDO") ?: "Fundo Desconocido"

        val localDataManager = LocalDataManager(this)

        val miToolbar = findViewById<Toolbar>(R.id.toolbarCanchas)
        setSupportActionBar(miToolbar)
        supportActionBar?.title = "Canchas fundo: $codigoFundo - $nombreFundo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val rvCanchas = findViewById<RecyclerView>(R.id.rvCanchas)
        rvCanchas.layoutManager = LinearLayoutManager(this)

        RetrofitClient.api.obtenerCanchas(codigoFundo)
            .enqueue(object : Callback<List<Cancha>> {
                override fun onResponse(call: Call<List<Cancha>>, response: Response<List<Cancha>>) {
                    if (response.isSuccessful) {
                        val listaCanchas = response.body() ?: emptyList()

                        if (listaCanchas.isEmpty()) {
                            Toast.makeText(this@CanchasActivity, "Aún no hay canchas registradas.", Toast.LENGTH_LONG).show()
                        } else {
                            val adapter = CanchaAdapter(listaCanchas)
                            rvCanchas.adapter = adapter

                            lifecycleScope.launch {
                                localDataManager.guardarCanchasPorFundo(codigoFundo, listaCanchas)
                            }
                        }
                    } else {
                        Toast.makeText(this@CanchasActivity, "Error en el servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Cancha>>, t: Throwable) {

                    lifecycleScope.launch {
                        val canchasOffline = localDataManager.leerCanchasPorFundo(codigoFundo)

                        if (canchasOffline.isNotEmpty()) {
                            Toast.makeText(this@CanchasActivity, "Sin señal. Mostrando canchas guardadas.", Toast.LENGTH_LONG).show()
                            val adapter = CanchaAdapter(canchasOffline)
                            rvCanchas.adapter = adapter
                        } else {
                            Toast.makeText(this@CanchasActivity, "Error de red y no hay datos guardados para este fundo.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}