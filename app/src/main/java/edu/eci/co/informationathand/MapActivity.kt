package edu.eci.co.informationathand

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Ubicación inicial
        val bogota = LatLng(4.6097, -74.0817)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 11f))

        // Marcadores de distintos colores con info
        val markers = listOf(
            Triple(LatLng(4.6097, -74.0817), "Accidente leve", "Hora: 8:30 AM - Cll 72 con 15"),
            Triple(LatLng(4.6486, -74.1079), "Choque múltiple", "Hora: 9:10 AM - Av. Boyacá"),
            Triple(LatLng(4.5981, -74.0760), "Vehículo varado", "Hora: 10:45 AM - Cll 26 con 7")
        )

        val colors = listOf(
            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_YELLOW
        )

        markers.forEachIndexed { index, triple ->
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(triple.first)
                    .title(triple.second)
                    .snippet("${triple.third}\nToca para ver más detalles")
                    .icon(BitmapDescriptorFactory.defaultMarker(colors[index]))
            )
            marker?.tag = index
        }

        mMap.setOnInfoWindowClickListener(this)
    }

    override fun onInfoWindowClick(marker: Marker) {
        val intent = Intent(this, DetalleReporteActivity::class.java)
        intent.putExtra("titulo", marker.title)
        intent.putExtra("detalle", marker.snippet)
        startActivity(intent)
    }
}
