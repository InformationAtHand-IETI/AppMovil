package edu.eci.co.informationathand

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.eci.co.informationathand.utils.StorageHelper
import java.text.SimpleDateFormat
import java.util.*

class MainMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storageHelper: StorageHelper

    private lateinit var fabMap: FloatingActionButton
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var mapFragment: SupportMapFragment

    // Botones de navegación
    private lateinit var navProfileBtn: LinearLayout
    private lateinit var navChatBtn: LinearLayout
    private lateinit var navAccidentsBtn: LinearLayout
    private lateinit var navPlansBtn: LinearLayout

    // Iconos y labels
    private lateinit var iconProfile: ImageView
    private lateinit var iconChat: ImageView
    private lateinit var iconAccidents: ImageView
    private lateinit var iconPlans: ImageView

    private lateinit var labelProfile: TextView
    private lateinit var labelChat: TextView
    private lateinit var labelAccidents: TextView
    private lateinit var labelPlans: TextView

    private var isMapVisible = true
    private var currentSelectedNav = "map"

    private val allMarkers = mutableListOf<Marker>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_map_navigation)

        storageHelper = StorageHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Views
        fabMap = findViewById(R.id.fab_map)
        fragmentContainer = findViewById(R.id.fragment_container)

        // Nav buttons
        navProfileBtn = findViewById(R.id.nav_profile_btn)
        navChatBtn = findViewById(R.id.nav_chat_btn)
        navAccidentsBtn = findViewById(R.id.nav_accidents_btn)
        navPlansBtn = findViewById(R.id.nav_plans_btn)

        // Icons
        iconProfile = findViewById(R.id.icon_profile)
        iconChat = findViewById(R.id.icon_chat)
        iconAccidents = findViewById(R.id.icon_accidents)
        iconPlans = findViewById(R.id.icon_plans)

        // Labels
        labelProfile = findViewById(R.id.label_profile)
        labelChat = findViewById(R.id.label_chat)
        labelAccidents = findViewById(R.id.label_accidents)
        labelPlans = findViewById(R.id.label_plans)

        // Mapa
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Navegación inferior
        setupNavigation()

        // FAB → siempre regresa al mapa
        fabMap.setOnClickListener {
            showMap()
        }

        // 🔥 NUEVO SISTEMA DE BOTÓN ATRÁS (reemplaza a onBackPressed())
        onBackPressedDispatcher.addCallback(this) {
            if (!isMapVisible) {
                showMap()
            } else {
                finish()
            }
        }
    }

    private fun setupNavigation() {
        navProfileBtn.setOnClickListener {
            selectNavItem("profile")
            showFragment(ProfileFragment())
        }

        navChatBtn.setOnClickListener {
            selectNavItem("chat")
            showFragment(ChatFragment())
        }

        navAccidentsBtn.setOnClickListener {
            selectNavItem("accidents")
            showFragment(AccidentsFragment())
        }

        navPlansBtn.setOnClickListener {
            selectNavItem("plans")
            showFragment(PlansFragment())
        }

        selectNavItem("map")
    }

    private fun selectNavItem(item: String) {
        currentSelectedNav = item

        val activeColor = Color.parseColor("#1A237E")
        val inactiveColor = Color.parseColor("#9E9E9E")

        // Reset all
        iconProfile.setColorFilter(inactiveColor)
        iconChat.setColorFilter(inactiveColor)
        iconAccidents.setColorFilter(inactiveColor)
        iconPlans.setColorFilter(inactiveColor)

        labelProfile.setTextColor(inactiveColor)
        labelChat.setTextColor(inactiveColor)
        labelAccidents.setTextColor(inactiveColor)
        labelPlans.setTextColor(inactiveColor)

        when (item) {
            "profile" -> {
                iconProfile.setColorFilter(activeColor)
                labelProfile.setTextColor(activeColor)
            }
            "chat" -> {
                iconChat.setColorFilter(activeColor)
                labelChat.setTextColor(activeColor)
            }
            "accidents" -> {
                iconAccidents.setColorFilter(activeColor)
                labelAccidents.setTextColor(activeColor)
            }
            "plans" -> {
                iconPlans.setColorFilter(activeColor)
                labelPlans.setTextColor(activeColor)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val bogota = LatLng(4.6097, -74.0817)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 11f))

        enableMyLocation()
        loadSampleMarkers()
        loadStoredReports()

        mMap.setOnInfoWindowClickListener(this)

        mMap.setOnMapLongClickListener { latLng ->
            showCreateReportDialog(latLng)
        }

        mMap.setOnMyLocationButtonClickListener {
            createReportAtCurrentLocation()
            true
        }

        // Manejo para centrar marcador desde fragmento
        intent?.let { intentData ->
            if (intentData.hasExtra("center_latitude")) {
                val lat = intentData.getDoubleExtra("center_latitude", 0.0)
                val lng = intentData.getDoubleExtra("center_longitude", 0.0)
                val title = intentData.getStringExtra("marker_title") ?: "Ubicación"
                val shouldZoom = intentData.getBooleanExtra("zoom_to_marker", false)

                val location = LatLng(lat, lng)

                if (shouldZoom) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))

                    android.os.Handler().postDelayed({
                        for (marker in allMarkers) {
                            if (marker.position.latitude == lat && marker.position.longitude == lng) {
                                marker.showInfoWindow()
                                break
                            }
                        }
                    }, 500)
                }
            }
        }
    }

    private fun showMap() {
        isMapVisible = true
        fragmentContainer.visibility = View.GONE
        mapFragment.view?.visibility = View.VISIBLE
        selectNavItem("map")
    }

    private fun showFragment(fragment: Fragment) {
        isMapVisible = false
        mapFragment.view?.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
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
            if (marker != null) allMarkers.add(marker)
        }
    }

    private fun loadStoredReports() {
        val reports = storageHelper.getAllReports()

        reports.forEach { report ->
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
            if (marker != null) allMarkers.add(marker)
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
                showCreateReportDialog(LatLng(it.latitude, it.longitude))
            } ?: run {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showCreateReportDialog(latLng: LatLng) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_report, null)

        val etTitulo = dialogView.findViewById<EditText>(R.id.etReportTitle)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etReportDescription)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnSendReport)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelReport)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnEnviar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()

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

            saveReport(latLng, titulo, descripcion)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveReport(latLng: LatLng, title: String, description: String) {
        storageHelper.saveReport(latLng.latitude, latLng.longitude, title, description)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = sdf.format(Date())

        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("📍 $title")
                .snippet("$description\n\n🕒 $fecha\n\n👆 Toca para ver detalles")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        )

        Toast.makeText(this, "✅ Reporte guardado exitosamente", Toast.LENGTH_SHORT).show()

        marker?.showInfoWindow()
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

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
