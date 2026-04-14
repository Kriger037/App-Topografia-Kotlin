package com.felipe.topografiaapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPref = getSharedPreferences("SesionTopografia", MODE_PRIVATE)
        if(sharedPref.getBoolean("isLoggedIn", false)){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etContrasena = findViewById<EditText>(R.id.etContrasena)
        val btnIniciarSesion = findViewById<Button>(R.id.btnIniciarSesion)

        btnIniciarSesion.setOnClickListener {
            val usuarioTexto = etUsuario.text.toString().trim()
            val contrasenaTexto = etContrasena.text.toString().trim()

            Log.d("LOGIN_TEST", "Capturado -> Usuario: [$usuarioTexto], Contraseña: [$contrasenaTexto]")
            // Validación de casillas vacías
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
                                // Login exitoso
                                val nombreReal = loginResponse.nombre_usuario

                                val editor = sharedPref.edit()
                                editor.putBoolean("isLoggedIn", true)
                                editor.putString("nombre_usuario", nombreReal)
                                editor.apply()

                                Toast.makeText(this@LoginActivity, "Bienvenido, $nombreReal", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("NOMBRE_USUARIO", nombreReal)
                                startActivity(intent)
                                finish()

                            } else {
                                // Login fallido (Contraseña incorrecta)
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