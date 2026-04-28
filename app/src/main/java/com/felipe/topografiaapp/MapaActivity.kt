package com.felipe.topografiaapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var canchaId: Int = -1

    private lateinit var localDataManager: LocalDataManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // Inicializamos el gestor
        localDataManager = LocalDataManager(this)

        //Configuración del Toolbar
        val miToolbar = findViewById<Toolbar>(R.id.toolbarMapa)
        setSupportActionBar(miToolbar)
        supportActionBar?.title = "Mapa Satelital - Terreno"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Se recupera el ID de la cancha de la activity anterior
        canchaId = intent.getIntExtra("CANCHA_ID", -1)

        //Inicialización del mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapaFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE //Vista del mapa en modo satelital
        mMap.uiSettings.isZoomControlsEnabled = true //Se agregan los botones +/- en el mapa

        mMap.setMaxZoomPreference(21f)

        activarCapaUbicacion()

        if (canchaId != -1){
            cargarPuntosEnMapa()
        } else {
            Toast.makeText(this, "Error: No se recibió el ID de la cancha", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarPuntosEnMapa(){
        RetrofitClient.api.obtenerPRs(canchaId).enqueue(object : Callback<List<PR>> {
            override fun onResponse(call: Call<List<PR>>, response: Response<List<PR>>){
                if (response.isSuccessful){
                    val listaPrs = response.body() ?: emptyList()

                    if (listaPrs.isNotEmpty()){

                        localDataManager.guardarPRsPorCancha(canchaId, listaPrs)
                        dibujarMarcadores(listaPrs)
                    }
                }
            }

            override fun onFailure(call: Call<List<PR>>, t: Throwable) {

                val prsOffline = localDataManager.leerPRsPorCancha(canchaId)

                if (prsOffline.isNotEmpty()) {
                    Toast.makeText(this@MapaActivity, "Sin señal. Dibujando puntos guardados.", Toast.LENGTH_LONG).show()
                    dibujarMarcadores(prsOffline)
                } else {
                    Toast.makeText(this@MapaActivity, "Error en red y no hay datos guardados.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // Función para dibujar marcadores y centrar cámara
    private fun dibujarMarcadores(listaPrs: List<PR>) {
        val builder = LatLngBounds.Builder()
        var puntosConCoordenadas = 0

        for (pr in listaPrs){
            if (pr.latitud != null && pr.longitud != null){
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

        if (puntosConCoordenadas > 0){
            val bounds = builder.build()
            val padding = 150
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        } else{
            Toast.makeText(this@MapaActivity, "Los PRs no tienen coordenadas geográficas", Toast.LENGTH_LONG).show()
        }
    }

    // Función para solicitar permisos y encender el GPS
    private fun activarCapaUbicacion() {
        // Verificamos si tenemos cualquiera de los dos permisos aprobados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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

    // Escucha la respuesta del usuario cuando se le pide permiso de ubicación
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

    override fun onSupportNavigateUp(): Boolean{
        onBackPressedDispatcher.onBackPressed()
        return(true)
    }
}