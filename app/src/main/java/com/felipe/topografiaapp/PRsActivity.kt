package com.felipe.topografiaapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PRsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prs)

        // Se reciben los datos enviados por la pantalla anterior
        val canchaId = intent.getIntExtra("CANCHA_ID", -1)
        val numeroCancha = intent.getStringExtra("NUMERO_CANCHA") ?: "Cancha Desconocida"
        val codigoFundo = intent.getStringExtra("CODIGO_FUNDO") ?: "Sin Código"
        val nombreFundo = intent.getStringExtra("NOMBRE_FUNDO") ?: "Fundo Desconocido"

        // Configuración de la barra superior
        val miToolbar = findViewById<Toolbar>(R.id.toolbarPRs)
        setSupportActionBar(miToolbar)
        supportActionBar?.title = "$codigoFundo - $nombreFundo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Configuración del título de la cancha
        val tvTitulo = findViewById<TextView>(R.id.tvTituloCancha)
        tvTitulo.text = "PRs $numeroCancha"

        // Configuración del botón para ver el mapa
        val btnVerMapa = findViewById<Button>(R.id.btnVerMapa)
        btnVerMapa.setOnClickListener {
            val intent = android.content.Intent(this, MapaActivity::class.java)
            intent.putExtra("CANCHA_ID", canchaId)
            startActivity(intent)
        }

        // Configuración de la lista
        val rvPRS = findViewById<RecyclerView>(R.id.rvPRs)
        rvPRS.layoutManager = LinearLayoutManager(this)

        // Llamada a PHP con Retrofit

        if (canchaId != -1){
            RetrofitClient.api.obtenerPRs(canchaId)
                .enqueue(object : Callback<List<PR>> {
                override fun onResponse(call: Call<List<PR>>, response: Response<List<PR>>){
                    if (response.isSuccessful){
                        val listaPRs = response.body() ?: emptyList()

                        if (listaPRs.isEmpty()){
                            Toast.makeText(this@PRsActivity, "Aun no hay PRs en esta cancha.", Toast.LENGTH_LONG).show()
                        } else{
                            val adapter = PRAdapter(listaPRs)
                            rvPRS.adapter = adapter
                        }
                    } else {
                        Toast.makeText(this@PRsActivity, "Error en el servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<PR>>, t: Throwable){
                    Toast.makeText(this@PRsActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else {
            Toast.makeText(this, "Error: No se recibió el ID de la cancha.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}