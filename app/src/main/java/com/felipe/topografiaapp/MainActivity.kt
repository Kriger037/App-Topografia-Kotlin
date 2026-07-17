package com.felipe.topografiaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipe.topografiaapp.databinding.ActivityMainBinding
import com.felipe.topografiaapp.domain.model.Fundo
import com.felipe.topografiaapp.presentation.common.UiState
import com.felipe.topografiaapp.presentation.fundos.FundosViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: FundosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarMain)

        val sharedPref = getSharedPreferences("SesionTopografia", MODE_PRIVATE)
        val nombreGuardado = sharedPref.getString("nombre_usuario", "Desconocido")
        binding.tvUsuarioLogueado.text = "Usuario: $nombreGuardado"

        binding.rvFundos.layoutManager = LinearLayoutManager(this)

        observarEstados()
        viewModel.cargarFundos()
    }

    private fun observarEstados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fundosState.collect { estado ->
                    when (estado) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            binding.rvFundos.adapter = FundoAdapter(estado.data)
                        }
                        is UiState.Error -> {
                            Snackbar.make(binding.root, estado.mensaje, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.estaOffline.collect { offline ->
                    if (offline) {
                        Toast.makeText(
                            this@MainActivity,
                            "Sin señal. Mostrando fundos guardados.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
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