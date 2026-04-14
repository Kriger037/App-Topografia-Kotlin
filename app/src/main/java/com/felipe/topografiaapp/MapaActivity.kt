package com.felipe.topografiaapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

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
                            Toast.makeText(this@MapaActivity, "Los PRs no tienen coordenadas geográficas",
                                Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<PR>>, t: Throwable) {
                Toast.makeText(this@MapaActivity, "Error en red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    override fun onSupportNavigateUp(): Boolean{
        onBackPressedDispatcher.onBackPressed()
        return(true)
    }
}