package com.felipe.topografiaapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipe.topografiaapp.databinding.ActivityPrsBinding
import com.felipe.topografiaapp.domain.model.CoordenadaResult
import com.felipe.topografiaapp.domain.model.PR
import com.felipe.topografiaapp.presentation.common.UiState
import com.felipe.topografiaapp.presentation.import_coords.ImportActivity
import com.felipe.topografiaapp.presentation.prs.PRsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PRsActivity : AppCompatActivity() {

    private val viewModel: PRsViewModel by viewModels()
    private lateinit var binding: ActivityPrsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val canchaId    = intent.getIntExtra("CANCHA_ID", -1)
        val numeroCancha = intent.getStringExtra("NUMERO_CANCHA") ?: ""
        val codigoFundo  = intent.getStringExtra("CODIGO_FUNDO") ?: ""
        val nombreFundo  = intent.getStringExtra("NOMBRE_FUNDO") ?: ""

        setSupportActionBar(binding.toolbarPRs)
        supportActionBar?.title = "$codigoFundo - $nombreFundo"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvTituloCancha.text = "PRs $numeroCancha"
        binding.rvPRs.layoutManager = LinearLayoutManager(this)

        observarEstados()

        if (canchaId != -1) viewModel.cargarPRs(canchaId)

        binding.btnVerMapa.setOnClickListener {
            startActivity(Intent(this, MapaActivity::class.java)
                .putExtra("CANCHA_ID", canchaId))
        }

        binding.btnImportarArchivo.setOnClickListener {
            val intent = Intent(this, ImportActivity::class.java)
            intent.putExtra("CANCHA_ID", canchaId)
            intent.putExtra("HUSO", 18) // por ahora fijo, después lo tomamos de la cancha
            intent.putExtra("NUMERO_CANCHA", numeroCancha)
            startActivity(intent)
        }
    }

    private fun observarEstados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prsState.collect { estado ->
                    binding.progressBarPRs.visibility =
                        if (estado is UiState.Loading) View.VISIBLE else View.GONE
                    when (estado) {
                        is UiState.Loading -> { }
                        is UiState.Success -> {
                            android.util.Log.d("PRsActivity", "Asignando adapter con ${estado.data.size} PRs")
                            binding.rvPRs.adapter = PRAdapter(estado.data)
                            android.util.Log.d("PRsActivity", "Adapter asignado, itemCount=${binding.rvPRs.adapter?.itemCount}")
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
                    binding.bannerOffline.visibility = if (offline) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}