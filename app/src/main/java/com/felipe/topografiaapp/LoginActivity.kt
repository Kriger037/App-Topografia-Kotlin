package com.felipe.topografiaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.felipe.topografiaapp.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.felipe.topografiaapp.data.remote.ApiService

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("SesionTopografia", MODE_PRIVATE)
        if (sharedPref.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnIniciarSesion.setOnClickListener {
            val usuarioTexto = binding.etUsuario.text.toString().trim()
            val contrasenaTexto = binding.etContrasena.text.toString().trim()

            Log.d("LOGIN_TEST", "Capturado -> Usuario: [$usuarioTexto]")

            if (usuarioTexto.isEmpty() || contrasenaTexto.isEmpty()) {
                Toast.makeText(this, "Por favor, completa ambos campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.api.iniciarSesion(usuarioTexto, contrasenaTexto)
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val loginResponse = response.body()!!
                            if (loginResponse.exito) {
                                val nombreReal = loginResponse.nombre_usuario ?: ""
                                val rol = loginResponse.rol ?: "User"

                                val editor = sharedPref.edit()
                                editor.putBoolean("isLoggedIn", true)
                                editor.putString("nombre_usuario", nombreReal)
                                editor.putString("rol", rol)
                                editor.apply()

                                Toast.makeText(this@LoginActivity, "Bienvenido, $nombreReal", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java)
                                    .putExtra("NOMBRE_USUARIO", nombreReal))
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, loginResponse.mensaje, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Error en el servidor", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}