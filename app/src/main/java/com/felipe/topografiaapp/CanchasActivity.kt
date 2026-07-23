package com.felipe.topografiaapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipe.topografiaapp.data.local.entity.PREntity
import com.felipe.topografiaapp.databinding.ActivityCanchasBinding
import com.felipe.topografiaapp.domain.model.Cancha
import com.felipe.topografiaapp.presentation.canchas.CanchasViewModel
import com.felipe.topografiaapp.presentation.common.UiState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CanchasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCanchasBinding
    private val viewModel: CanchasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCanchasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val codigoFundo = intent.getStringExtra("CODIGO_FUNDO") ?: "Sin Código"
        val nombreFundo = intent.getStringExtra("NOMBRE_FUNDO") ?: "Fundo Desconocido"

        setSupportActionBar(binding.toolbarCanchas)
        supportActionBar?.title = "Canchas fundo: $codigoFundo - $nombreFundo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.rvCanchas.layoutManager = LinearLayoutManager(this)

        observarEstados()
        viewModel.cargarCanchas(codigoFundo)
    }

    private fun observarEstados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.canchasState.collect { estado ->
                    when (estado) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            binding.rvCanchas.adapter = CanchaAdapter(estado.data) { cancha ->
                                descargarPRsDeCancha(cancha)
                            }
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
                        Snackbar.make(
                            binding.root,
                            "Sin señal. Mostrando canchas guardadas.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun descargarPRsDeCancha(cancha: Cancha) {
        Toast.makeText(this, "Descargando puntos para terreno...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val listaDtos = RetrofitClient.api.obtenerPRs(cancha.id)
                if (listaDtos.isNotEmpty()) {
                    val listaEntities = listaDtos.map { dto ->
                        PREntity(
                            id = dto.id,
                            canchaId = cancha.id,
                            descriptor = dto.descriptor,
                            norte = dto.norte,
                            este = dto.este,
                            cota = dto.cota,
                            latitud = dto.latitud,
                            longitud = dto.longitud,
                            fechaCreacion = dto.fecha_creacion,
                            fechaModificacion = dto.fecha_modificacion
                        )
                    }
                    viewModel.guardarPRsLocalmente(listaEntities, cancha.id)
                    Toast.makeText(
                        this@CanchasActivity,
                        "¡Listo! Puntos de ${cancha.numeroCancha} guardados.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@CanchasActivity,
                        "Esta cancha no tiene puntos topográficos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CanchasActivity,
                    "Error al descargar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}