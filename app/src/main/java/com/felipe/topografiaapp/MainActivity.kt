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
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.felipe.topografiaapp.databinding.ActivityMainBinding
import com.felipe.topografiaapp.domain.model.Fundo
import com.felipe.topografiaapp.presentation.common.UiState
import com.felipe.topografiaapp.presentation.fundos.FundosViewModel
import com.felipe.topografiaapp.worker.SyncWorker
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

    private fun sincronizarDatos() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)

        workManager.enqueueUniqueWork(
            "sync_prs_manual",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )

        // Observar el estado del worker para dar feedback al usuario
        workManager.getWorkInfoByIdLiveData(syncRequest.id)
            .observe(this) { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.ENQUEUED -> {
                        Toast.makeText(this, "Sincronización en cola...", Toast.LENGTH_SHORT).show()
                    }
                    WorkInfo.State.RUNNING -> {
                        Toast.makeText(this, "Sincronizando datos...", Toast.LENGTH_SHORT).show()
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        Toast.makeText(this, "Datos sincronizados correctamente", Toast.LENGTH_LONG).show()
                    }
                    WorkInfo.State.FAILED -> {
                        Toast.makeText(this, "Error al sincronizar. Intenta de nuevo.", Toast.LENGTH_LONG).show()
                    }
                    WorkInfo.State.BLOCKED -> {
                        Toast.makeText(this, "Sin conexión. Conecta a internet e intenta de nuevo.", Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sync -> {
                sincronizarDatos()
                true
            }
            R.id.action_logout -> {
                val sharedPref = getSharedPreferences("SesionTopografia", MODE_PRIVATE)
                sharedPref.edit().clear().apply()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}