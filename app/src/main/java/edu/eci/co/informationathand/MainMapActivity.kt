package edu.eci.co.informationathand

import CommunityChatsFragment
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.*

// --- SECCIÓN DE RED (RETROFIT) ---

data class DenunciaResponse(
    val id: String,
    val nombreDenunciante: String,
    val latitud: Double,
    val longitud: Double,
    val tipo: String,
    val descripciones: List<String>,
    val conteo: Int,
    val estado: String,
    val fechaCreacion: String?,
    val fechaActualizacion: String?,
    val fechaExpiracion: String?
)

data class DenunciaRequest(
    val nombreDenunciante: String,
    val latitud: Double,
    val longitud: Double,
    val tipo: String,
    val descripcion: String
)

interface ApiService {
    @GET("api/denuncias")
    suspend fun obtenerDenuncias(): List<DenunciaResponse>

    @POST("api/denuncias")
    suspend fun crearDenuncia(@Body denuncia: DenunciaRequest): DenunciaResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://parchesecgateway.azure-api.net/denuncias/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
// ---------------------------------

class MainMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storageHelper: StorageHelper

    // Variable para controlar el ciclo de actualización de 15s
    private var updateJob: Job? = null

    // Botones Flotantes
    private lateinit var fabMap: FloatingActionButton
    private lateinit var fabAddReport: FloatingActionButton

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

        // Inicializar Views
        fabMap = findViewById(R.id.fab_map)
        fabAddReport = findViewById(R.id.fab_add_report)
        fragmentContainer = findViewById(R.id.fragment_container)

        // Nav buttons
        navProfileBtn = findViewById(R.id.nav_profile_btn)
        navChatBtn = findViewById(R.id.nav_chat_btn)
        navAccidentsBtn = findViewById(R.id.nav_accidents_btn)
        navPlansBtn = findViewById(R.id.nav_plans_btn)

        // Icons y Labels
        iconProfile = findViewById(R.id.icon_profile)
        iconChat = findViewById(R.id.icon_chat)
        iconAccidents = findViewById(R.id.icon_accidents)
        iconPlans = findViewById(R.id.icon_plans)
        labelProfile = findViewById(R.id.label_profile)
        labelChat = findViewById(R.id.label_chat)
        labelAccidents = findViewById(R.id.label_accidents)
        labelPlans = findViewById(R.id.label_plans)

        // Mapa
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupNavigation()

        fabMap.setOnClickListener { showMap() }

        fabAddReport.setOnClickListener { createReportAtCurrentLocation() }

        onBackPressedDispatcher.addCallback(this) {
            if (!isMapVisible) showMap() else finish()
        }
    }

    // --- CICLO DE VIDA PARA AUTOMATIZAR EL REFRESH ---
    override fun onResume() {
        super.onResume()
        startRepeatingUpdates() // Comienza a actualizar cada 15s al abrir la app
    }

    override fun onPause() {
        super.onPause()
        stopRepeatingUpdates() // Detiene la actualización al salir para ahorrar batería
    }

    private fun startRepeatingUpdates() {
        updateJob?.cancel() // Cancela cualquier trabajo previo
        updateJob = lifecycleScope.launch {
            while (isActive) {
                // Solo actualiza si el mapa es visible y ya está inicializado
                if (isMapVisible && ::mMap.isInitialized) {
                    Log.d("AUTO_UPDATE", "Consultando nuevos reportes...")
                    loadReportsFromBackend()
                }
                delay(15000) // Espera 15 segundos
            }
        }
    }

    private fun stopRepeatingUpdates() {
        updateJob?.cancel()
    }
    // -------------------------------------------------

    private fun setupNavigation() {
        navProfileBtn.setOnClickListener {
            selectNavItem("profile")
            showFragment(ProfileFragment())
        }
        navChatBtn.setOnClickListener {
            selectNavItem("chat")
            showFragment(CommunityChatsFragment())
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

        // Reset
        iconProfile.setColorFilter(inactiveColor); labelProfile.setTextColor(inactiveColor)
        iconChat.setColorFilter(inactiveColor); labelChat.setTextColor(inactiveColor)
        iconAccidents.setColorFilter(inactiveColor); labelAccidents.setTextColor(inactiveColor)
        iconPlans.setColorFilter(inactiveColor); labelPlans.setTextColor(inactiveColor)

        // Active
        when (item) {
            "profile" -> { iconProfile.setColorFilter(activeColor); labelProfile.setTextColor(activeColor) }
            "chat" -> { iconChat.setColorFilter(activeColor); labelChat.setTextColor(activeColor) }
            "accidents" -> { iconAccidents.setColorFilter(activeColor); labelAccidents.setTextColor(activeColor) }
            "plans" -> { iconPlans.setColorFilter(activeColor); labelPlans.setTextColor(activeColor) }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val bogota = LatLng(4.6097, -74.0817)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bogota, 11f))

        enableMyLocation()

        mMap.setOnInfoWindowClickListener(this)

        mMap.setOnMapLongClickListener { latLng ->
            showCreateReportDialog(latLng)
        }

        // Nota: loadReportsFromBackend() se llamará automáticamente desde onResume() -> startRepeatingUpdates()
        // así que no es estrictamente necesario llamarlo aquí, pero si quieres carga inmediata al milisegundo:
        // loadReportsFromBackend()
    }

    private fun loadReportsFromBackend() {
        lifecycleScope.launch {
            try {
                val denuncias = RetrofitClient.instance.obtenerDenuncias()

                // Verificamos si el mapa sigue vivo antes de tocar la UI
                if (!::mMap.isInitialized) return@launch

                mMap.clear()
                allMarkers.clear()

                denuncias.forEach { denuncia ->
                    val colorIcono = obtenerColorPorTipo(denuncia.tipo)

                    val tituloMarker = if(denuncia.conteo > 1) {
                        "📍 ${denuncia.tipo} (+${denuncia.conteo})"
                    } else {
                        "📍 ${denuncia.tipo}"
                    }

                    val detalleTexto = if (denuncia.descripciones.isNotEmpty()) {
                        denuncia.descripciones[0]
                    } else {
                        "Sin descripción"
                    }

                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(denuncia.latitud, denuncia.longitud))
                            .title(tituloMarker)
                            .snippet("$detalleTexto\n👆 Ver detalle completo")
                            .icon(BitmapDescriptorFactory.defaultMarker(colorIcono))
                    )
                    marker?.tag = denuncia
                    if (marker != null) allMarkers.add(marker)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("MAP_ERROR", "Error cargando reportes: ${e.message}")
            }
        }
    }

    private fun obtenerColorPorTipo(tipo: String): Float {
        return when (tipo.uppercase()) {
            "GRAVE", "ACCIDENTE" -> BitmapDescriptorFactory.HUE_RED
            "LEVE" -> BitmapDescriptorFactory.HUE_YELLOW
            "CHOQUE", "MEDIO" -> BitmapDescriptorFactory.HUE_ORANGE
            "PROTESTA" -> BitmapDescriptorFactory.HUE_VIOLET
            else -> BitmapDescriptorFactory.HUE_CYAN
        }
    }

    private fun showMap() {
        isMapVisible = true
        fragmentContainer.visibility = View.GONE
        mapFragment.view?.visibility = View.VISIBLE
        selectNavItem("map")
        showBottomNav()
        fabAddReport.visibility = View.VISIBLE
    }

    private fun showFragment(fragment: Fragment) {
        isMapVisible = false
        mapFragment.view?.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        fabAddReport.visibility = View.GONE

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

    private fun createReportAtCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación necesario", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                showCreateReportDialog(LatLng(it.latitude, it.longitude))
            } ?: run {
                Toast.makeText(this, "No se pudo obtener ubicación GPS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun hideBottomNav() {
        findViewById<View>(R.id.bottom_nav_container).visibility = View.GONE
    }

    fun showBottomNav() {
        findViewById<View>(R.id.bottom_nav_container).visibility = View.VISIBLE
    }

    private fun showCreateReportDialog(latLng: LatLng) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_report, null)

        val spTipo = dialogView.findViewById<Spinner>(R.id.spReportType)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etReportDescription)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnSendReport)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelReport)

        val tiposValidos = listOf("GRAVE", "MEDIO", "LEVE", "PROTESTA")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposValidos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipo.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnEnviar.setOnClickListener {
            val tipoSeleccionado = spTipo.selectedItem.toString()
            val descripcion = etDescripcion.text.toString().trim()

            if (descripcion.length < 10) {
                etDescripcion.error = "Muy corta: Mínimo 10 caracteres."
                etDescripcion.requestFocus()
                return@setOnClickListener
            }

            if (descripcion.length > 500) {
                etDescripcion.error = "Muy larga: Máximo 500 caracteres."
                etDescripcion.requestFocus()
                return@setOnClickListener
            }

            saveReportToBackend(latLng, tipoSeleccionado, descripcion)
            dialog.dismiss()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveReportToBackend(latLng: LatLng, tipo: String, descripcion: String) {
        Toast.makeText(this, "Enviando...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val nombreUsuario = storageHelper.getUserName() ?: "Usuario Anónimo"

                val nuevoReporte = DenunciaRequest(
                    nombreDenunciante = nombreUsuario,
                    latitud = latLng.latitude,
                    longitud = latLng.longitude,
                    tipo = tipo,
                    descripcion = descripcion
                )

                RetrofitClient.instance.crearDenuncia(nuevoReporte)

                Toast.makeText(this@MainMapActivity, "✅ Reporte enviado por $nombreUsuario", Toast.LENGTH_SHORT).show()

                // Recargar mapa inmediatamente para ver el propio reporte
                loadReportsFromBackend()

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            } catch (e: HttpException) {
                val errorMsg = e.response()?.errorBody()?.string()
                Log.e("API_ERROR", "Error: $errorMsg")
                Toast.makeText(this@MainMapActivity, "Error al enviar reporte", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainMapActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        val denuncia = marker.tag as? DenunciaResponse
        val intent = Intent(this, DetalleReporteActivity::class.java)

        if (denuncia != null) {
            intent.putExtra("tipo", denuncia.tipo)
            intent.putExtra("estado", denuncia.estado)
            intent.putExtra("conteo", denuncia.conteo)
            intent.putExtra("descripciones", denuncia.descripciones.joinToString("\n"))
            intent.putExtra("fecha", denuncia.fechaCreacion)
            intent.putExtra("autor", denuncia.nombreDenunciante)
        } else {
            intent.putExtra("tipo", marker.title)
            intent.putExtra("descripciones", marker.snippet)
        }
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        }
    }
}