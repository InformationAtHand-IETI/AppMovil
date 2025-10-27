package edu.eci.co.informationathand

import android.widget.TextView
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.eci.co.informationathand.utils.StorageHelper
import android.view.Menu
import android.view.MenuItem
import java.text.SimpleDateFormat
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storageHelper: StorageHelper

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Inicializar StorageHelper y FusedLocationClient
        storageHelper = StorageHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Ubicación inicial (Bogotá)
        val bogota = LatLng(4.6097, -74.0817)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 11f))

        // Habilitar ubicación del usuario
        enableMyLocation()

        // Cargar reportes de ejemplo (los que tenías originalmente)
        loadSampleMarkers()

        // Cargar reportes guardados del storage
        loadStoredReports()

        // Click en ventana de información para ver detalles
        mMap.setOnInfoWindowClickListener(this)

        // Click largo para crear nuevo reporte
        mMap.setOnMapLongClickListener { latLng ->
            showCreateReportDialog(latLng)
        }

        // Click en botón de ubicación para crear reporte en ubicación actual
        mMap.setOnMyLocationButtonClickListener {
            createReportAtCurrentLocation()
            true
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun loadSampleMarkers() {
        // Marcadores de ejemplo originales
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
                    .snippet("${triple.third}\n\n📍 Toca para ver más detalles")
                    .icon(BitmapDescriptorFactory.defaultMarker(colors[index]))
            )
            marker?.tag = "sample_$index"
        }
    }

    private fun loadStoredReports() {
        val reports = storageHelper.getAllReports()

        reports.forEach { report ->
            // Formatear fecha
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fecha = sdf.format(Date(report.timestamp))

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(report.latitude, report.longitude))
                    .title("📍 ${report.title}")
                    .snippet("${report.description}\n\n🕒 $fecha\n\n👆 Toca para ver detalles")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            marker?.tag = "stored_${report.id}"
        }
    }

    private fun createReportAtCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permiso de ubicación necesario", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                showCreateReportDialog(latLng)
            } ?: run {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCreateReportDialog(latLng: LatLng) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_report, null)

        val tvDireccion = dialogView.findViewById<TextView>(R.id.tvReportAddress)
        val etTitulo = dialogView.findViewById<EditText>(R.id.etReportTitle)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etReportDescription)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnSendReport)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelReport)

        // Obtener la dirección desde las coordenadas
        val geocoder = android.location.Geocoder(this, Locale.getDefault())
        try {
            val direcciones = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!direcciones.isNullOrEmpty()) {
                val direccion = direcciones[0].getAddressLine(0)
                tvDireccion.text = "📍 $direccion"
            } else {
                tvDireccion.text = "📍 Dirección no disponible"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tvDireccion.text = "📍 Error obteniendo dirección"
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnEnviar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val direccionTexto = tvDireccion.text.toString()

            if (titulo.isEmpty()) {
                etTitulo.error = "Ingresa un título"
                etTitulo.requestFocus()
                return@setOnClickListener
            }

            if (descripcion.isEmpty()) {
                etDescripcion.error = "Ingresa una descripción"
                etDescripcion.requestFocus()
                return@setOnClickListener
            }

            // Guardar el reporte junto con la dirección
            saveReport(latLng, titulo, "$descripcion\n\n$direccionTexto")
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }


    private fun saveReport(latLng: LatLng, title: String, description: String) {
        // Guardar reporte en storage
        storageHelper.saveReport(latLng.latitude, latLng.longitude, title, description)

        // Formatear fecha
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = sdf.format(Date())

        // Agregar marcador al mapa con ícono de ubicación
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("📍 $title")
                .snippet("$description\n\n🕒 $fecha\n\n👆 Toca para ver detalles")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        )

        Toast.makeText(this, "✅ Reporte guardado exitosamente", Toast.LENGTH_SHORT).show()

        // Mostrar la ventana de información del nuevo marcador
        marker?.showInfoWindow()

        // Animar cámara al nuevo marcador
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    override fun onInfoWindowClick(marker: Marker) {
        val intent = Intent(this, DetalleReporteActivity::class.java)
        intent.putExtra("titulo", marker.title)
        intent.putExtra("detalle", marker.snippet)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                storageHelper.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}