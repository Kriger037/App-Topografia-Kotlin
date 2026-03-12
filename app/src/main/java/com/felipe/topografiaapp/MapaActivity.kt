package com.felipe.topografiaapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        //Configuración del Toolbar
        val miToolbar = findViewById<Toolbar>(R.id.toolbarMapa)
        setSupportActionBar(miToolbar)
        supportActionBar?.title = "Mapa Satelital - Terreno"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Inicialización del mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapaFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE //Vista del mapa en modo satelital
        val coordenadaPrueba = LatLng(-37.594953, -72.279986) //Coordenada de prueba
        mMap.addMarker(MarkerOptions().position(coordenadaPrueba).title("Punto de Prueba")) //Marcador de las coordenadas de prueba
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadaPrueba, 12f)) //Movimiento automatico de la camara a las coordenadas de prueba
    }

    override fun onSupportNavigateUp(): Boolean{
        onBackPressedDispatcher.onBackPressed()
        return(true)
    }
}