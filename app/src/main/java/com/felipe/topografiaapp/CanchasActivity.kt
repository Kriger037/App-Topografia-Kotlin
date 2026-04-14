package com.felipe.topografiaapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CanchasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canchas)

        // Recibir datos del Fundo seleccionado
        val codigoFundo = intent.getStringExtra("CODIGO_FUNDO") ?: "Sin Código"
        val nombreFundo = intent.getStringExtra("NOMBRE_FUNDO") ?: "Fundo Desconocido"

        // Configurar la barra superior
        val miToolbar = findViewById<Toolbar>(R.id.toolbarCanchas)
        setSupportActionBar(miToolbar)
        supportActionBar?.title = "Canchas fundo: $codigoFundo - $nombreFundo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configurar la lista (RecyclerView)
        val rvCanchas = findViewById<RecyclerView>(R.id.rvCanchas)
        rvCanchas.layoutManager = LinearLayoutManager(this)

        // Llamada a PHP con Retrofit
        RetrofitClient.api.obtenerCanchas(codigoFundo)
            .enqueue(object : Callback<List<Cancha>> {
            override fun onResponse(call: Call<List<Cancha>>, response: Response<List<Cancha>>) {
                if (response.isSuccessful) {
                    val listaCanchas = response.body() ?: emptyList()

                    if (listaCanchas.isEmpty()) {
                        Toast.makeText(this@CanchasActivity, "Aún no hay canchas registradas.", Toast.LENGTH_LONG).show()
                    } else {
                        // Si hay datos se dibujan las tarjetas
                        val adapter = CanchaAdapter(listaCanchas)
                        rvCanchas.adapter = adapter
                    }
                } else {
                    Toast.makeText(this@CanchasActivity, "Error en el servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Cancha>>, t: Throwable) {
                Toast.makeText(this@CanchasActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Lógica de la flecha de retroceso
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}