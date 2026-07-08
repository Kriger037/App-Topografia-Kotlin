package com.felipe.topografiaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.felipe.topografiaapp.data.local.entity.FundoEntity

class MainActivity : AppCompatActivity() {

    private lateinit var localDataManager: LocalDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        localDataManager = LocalDataManager(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarMain)
        setSupportActionBar(toolbar)

        val tvUsuario = findViewById<TextView>(R.id.tvUsuarioLogueado)
        val sharedPref = getSharedPreferences("SesionTopografia", MODE_PRIVATE)
        val nombreGuardado = sharedPref.getString("nombre_usuario", "Desconocido")
        tvUsuario.text = "Usuario: $nombreGuardado"

        val rvFundos = findViewById<RecyclerView>(R.id.rvFundos)
        rvFundos.layoutManager = LinearLayoutManager(this)

        RetrofitClient.api.obtenerUsuarios()
            .enqueue(object : Callback<List<Usuario>> {
                override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                    if (response.isSuccessful) {
                        val usuarios = response.body()
                        Log.d("API_TEST", "Conexión exitosa. Usuarios encontrados: ${usuarios?.size}")
                    } else {
                        Log.e("API_TEST", "Error en la respuesta: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                    Log.e("API_TEST", "Falló la conexión: ${t.message}")
                }
            })

        RetrofitClient.api.obtenerFundos()
            .enqueue(object : Callback<List<Fundo>> {
                override fun onResponse(call: Call<List<Fundo>>, response: Response<List<Fundo>>) {
                    if (response.isSuccessful) {
                        val fundos = response.body() ?: emptyList()
                        Log.d("API_FUNDOS", "Fundos encontrados: ${fundos.size}")

                        // Convertir de Fundo (modelo viejo) a FundoEntity (modelo nuevo)
                        val listaEntities = fundos.map { fundo ->
                            FundoEntity(
                                id = fundo.id,
                                codigoFundo = fundo.codigo_fundo,
                                nombreFundo = fundo.nombre_fundo,
                                comuna = fundo.comuna
                            )
                        }

                        lifecycleScope.launch {
                            localDataManager.guardarFundos(listaEntities)
                        }

                        // FundoAdapter sigue recibiendo el modelo viejo por ahora
                        val adaptador = FundoAdapter(fundos)
                        rvFundos.adapter = adaptador

                    } else {
                        Log.e("API_FUNDOS", "Error en la respuesta: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Fundo>>, t: Throwable) {
                    Log.e("API_FUNDOS", "Falló la red: ${t.message}")

                    lifecycleScope.launch {
                        val fundosOffline = localDataManager.leerFundos()
                        if (fundosOffline.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "Modo Offline: Cargando fundos guardados",
                                Toast.LENGTH_LONG
                            ).show()

                            // Convertir de FundoEntity a Fundo para que FundoAdapter lo acepte
                            val fundosParaAdapter = fundosOffline.map { entity ->
                                Fundo(
                                    id = entity.id,
                                    codigo_fundo = entity.codigoFundo,
                                    nombre_fundo = entity.nombreFundo,
                                    comuna = entity.comuna
                                )
                            }

                            val adaptador = FundoAdapter(fundosParaAdapter)
                            rvFundos.adapter = adaptador
                        }
                    }
                }
            })
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            val sharedPref = getSharedPreferences("SesionTopografia", MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}