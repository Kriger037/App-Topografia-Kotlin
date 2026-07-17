package com.felipe.topografiaapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.felipe.topografiaapp.domain.model.PR
import com.felipe.topografiaapp.presentation.common.UiState
import com.felipe.topografiaapp.presentation.mapa.MapaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var canchaId: Int = -1
    private val viewModel: MapaViewModel by viewModels()
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        val miToolbar = findViewById<Toolbar>(R.id.toolbarMapa)
        setSupportActionBar(miToolbar)
        supportActionBar?.title = "Mapa Satelital - Terreno"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        canchaId = intent.getIntExtra("CANCHA_ID", -1)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapaFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setMaxZoomPreference(21f)

        activarCapaUbicacion()

        if (canchaId != -1) {
            observarEstados()
            viewModel.cargarPRsParaMapa(canchaId)
        } else {
            Toast.makeText(this, "Error: No se recibió el ID de la cancha", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observarEstados() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prsState.collect { estado ->
                    when (estado) {
                        is UiState.Loading -> {}
                        is UiState.Success -> dibujarMarcadores(estado.data)
                        is UiState.Error -> {
                            Toast.makeText(this@MapaActivity, estado.mensaje, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun dibujarMarcadores(listaPrs: List<PR>) {
        val builder = LatLngBounds.Builder()
        var puntosConCoordenadas = 0

        for (pr in listaPrs) {
            if (pr.latitud != null && pr.longitud != null) {
                val posicion = LatLng(pr.latitud, pr.longitud)
                mMap.addMarker(
                    MarkerOptions()
                        .position(posicion)
                        .title(pr.descriptor)
                        .snippet("Cota: ${String.format("%.3f", pr.cota)}")
                )
                builder.include(posicion)
                puntosConCoordenadas++
            }
        }

        if (puntosConCoordenadas > 0) {
            val bounds = builder.build()
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        } else {
            Toast.makeText(this, "Los PRs no tienen coordenadas geográficas", Toast.LENGTH_LONG).show()
        }
    }

    private fun activarCapaUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activarCapaUbicacion()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}