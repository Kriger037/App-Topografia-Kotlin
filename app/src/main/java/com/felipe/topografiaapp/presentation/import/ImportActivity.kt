package com.felipe.topografiaapp.presentation.import_coords

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.felipe.topografiaapp.databinding.ActivityImportBinding
import com.felipe.topografiaapp.domain.model.FormatoArchivoCoord
import com.felipe.topografiaapp.domain.model.ResultadoImportacion
import com.felipe.topografiaapp.presentation.common.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportBinding
    private val viewModel: ImportViewModel by viewModels()

    private var canchaId: Int = -1

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.procesarArchivoDesdeUri(it, canchaId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        canchaId = intent.getIntExtra("CANCHA_ID", -1)
        val numeroCancha = intent.getStringExtra("NUMERO_CANCHA") ?: ""

        setSupportActionBar(binding.toolbarImport)
        supportActionBar?.title = "Importar PRs — $numeroCancha"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.tvRutaCarpeta.text = viewModel.rutaCarpeta

        val archivos = viewModel.archivosDisponibles
        if (archivos.isEmpty()) {
            binding.tvSinArchivos.visibility = View.VISIBLE
            binding.rvArchivos.visibility = View.GONE
        } else {
            binding.tvSinArchivos.visibility = View.GONE
            binding.rvArchivos.visibility = View.VISIBLE
            binding.rvArchivos.layoutManager = LinearLayoutManager(this)
            binding.rvArchivos.adapter = ArchivoAdapter(archivos) { nombreArchivo ->
                viewModel.procesarArchivoDesdeNombre(nombreArchivo, canchaId)
            }
        }

        binding.btnElegirArchivo.setOnClickListener {
            pickFileLauncher.launch("*/*")
        }

        binding.btnConfirmarImportacion.setOnClickListener {
            if (canchaId != -1) {
                viewModel.confirmarImportacion(canchaId)
            }
        }

        observarEstados()
    }

    private fun observarEstados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.importState.collect { estado ->
                    when (estado) {
                        is UiState.Loading -> {
                            binding.progressBarImport.visibility = View.VISIBLE
                            binding.layoutVistaPrevia.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            binding.progressBarImport.visibility = View.GONE
                            mostrarVistaPrevia(estado.data)
                        }
                        is UiState.Error -> {
                            binding.progressBarImport.visibility = View.GONE
                            Toast.makeText(this@ImportActivity, estado.mensaje, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.guardadoState.collect { estado ->
                    when (estado) {
                        is UiState.Loading -> {}
                        is UiState.Success -> {
                            Toast.makeText(
                                this@ImportActivity,
                                "${estado.data} puntos importados correctamente",
                                Toast.LENGTH_LONG
                            ).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                        is UiState.Error -> {
                            Toast.makeText(this@ImportActivity, estado.mensaje, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun mostrarVistaPrevia(resultado: ResultadoImportacion) {
        binding.layoutVistaPrevia.visibility = View.VISIBLE

        val formatoTexto = when (resultado.formatoDetectado) {
            FormatoArchivoCoord.UTM_ZONA_18 -> "Formato detectado: UTM Zona 18 Sur"
            FormatoArchivoCoord.UTM_ZONA_19 -> "Formato detectado: UTM Zona 19 Sur"
            FormatoArchivoCoord.LAT_LNG -> "Formato detectado: Latitud / Longitud"
            FormatoArchivoCoord.DESCONOCIDO -> "Formato no reconocido"
        }

        binding.tvFormatoDetectado.text = formatoTexto
        binding.tvPuntosDetectados.text = "Puntos válidos detectados: ${resultado.puntosValidos.size}"
        binding.tvLineasIgnoradas.text = "Líneas ignoradas: ${resultado.lineasIgnoradas}"

        val muestra = resultado.puntosValidos.take(5).joinToString("\n") { pr ->
            "${pr.descriptor}  N:${String.format("%.3f", pr.norte)}  E:${String.format("%.3f", pr.este)}  C:${String.format("%.3f", pr.cota)}"
        }
        binding.tvMuestraPuntos.text = if (muestra.isNotEmpty()) muestra else "Sin puntos válidos"

        if (resultado.puntosValidos.isEmpty()) {
            binding.btnConfirmarImportacion.isEnabled = false
            binding.btnConfirmarImportacion.alpha = 0.5f
        } else {
            binding.btnConfirmarImportacion.isEnabled = true
            binding.btnConfirmarImportacion.alpha = 1.0f
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}