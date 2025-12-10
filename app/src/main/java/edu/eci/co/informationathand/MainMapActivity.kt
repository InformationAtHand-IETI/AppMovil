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
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.*

// --- SECCIÓN DE RED (RETROFIT) ---

// 1. Modelo de Respuesta (Lo que recibimos de Azure)
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

// 2. Modelo de Petición (Lo que enviamos al crear)
data class DenunciaRequest(
    val nombreDenunciante: String,
    val latitud: Double,
    val longitud: Double,
    val tipo: String,        // Debe ser GRAVE, LEVE, MEDIO, PROTESTA
    val descripcion: String
)

// 3. Interfaz
interface ApiService {
    @GET("api/denuncias")
    suspend fun obtenerDenuncias(): List<DenunciaResponse>

    @POST("api/denuncias")
    suspend fun crearDenuncia(@Body denuncia: DenunciaRequest): DenunciaResponse
}

// 4. Cliente Singleton
object RetrofitClient {
    private const val BASE_URL = "https://parchedenuncia-e8hyayg0fuavahc2.centralus-01.azurewebsites.net/"

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

    // Botones Flotantes
    private lateinit var fabMap: FloatingActionButton        // Centrar
    private lateinit var fabAddReport: FloatingActionButton  // Nuevo Reporte (Rojo)

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
        fabAddReport = findViewById(R.id.fab_add_report) // NUEVO BOTÓN
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

        // Navegación inferior
        setupNavigation()

        // ACCIÓN BOTÓN CENTRAR MAPA
        fabMap.setOnClickListener {
            showMap()
        }

        // ACCIÓN BOTÓN REPORTAR (ROJO)
        fabAddReport.setOnClickListener {
            createReportAtCurrentLocation()
        }

        // Sistema Back
        onBackPressedDispatcher.addCallback(this) {
            if (!isMapVisible) showMap() else finish()
        }
    }

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

        // Opción alternativa: Long Click para reportar manualmente en otro lado
        mMap.setOnMapLongClickListener { latLng ->
            showCreateReportDialog(latLng)
        }

        // Cargar datos al iniciar
        loadReportsFromBackend()
    }

    private fun loadReportsFromBackend() {
        lifecycleScope.launch {
            try {
                val denuncias = RetrofitClient.instance.obtenerDenuncias()

                // --- ARREGLO 1: LIMPIAR EL MAPA ANTES DE PINTAR ---
                mMap.clear()       // Borra los marcadores visuales viejos
                allMarkers.clear() // Borra la lista de memoria
                // --------------------------------------------------

                denuncias.forEach { denuncia ->
                    val colorIcono = obtenerColorPorTipo(denuncia.tipo)

                    // Mostrar conteo si hay más de 1 reporte agrupado
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
        fabAddReport.visibility = View.VISIBLE  //
    }

    private fun showFragment(fragment: Fragment) {
        isMapVisible = false
        mapFragment.view?.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        fabAddReport.visibility = View.GONE  //

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

        // Obtener ubicación GPS y abrir diálogo
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

    // --- DIÁLOGO CON SPINNER PARA EVITAR ERROR 400 ---
    // Reemplaza esta función completa en MainMapActivity.kt
    private fun showCreateReportDialog(latLng: LatLng) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_report, null)

        // Referencias
        val spTipo = dialogView.findViewById<Spinner>(R.id.spReportType)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etReportDescription)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnSendReport)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelReport)

        // Llenar Spinner
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

            // --- 🔍 VALIDACIÓN DE LONGITUD (10 a 500 caracteres) ---

            if (descripcion.length < 10) {
                etDescripcion.error = "Muy corta: Mínimo 10 caracteres."
                etDescripcion.requestFocus() // Pone el cursor ahí para que escriban más
                return@setOnClickListener
            }

            if (descripcion.length > 500) {
                etDescripcion.error = "Muy larga: Máximo 500 caracteres."
                etDescripcion.requestFocus()
                return@setOnClickListener
            }

            // -------------------------------------------------------

            // Si pasa la validación, enviamos:
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
                // 1. OBTENER EL NOMBRE REAL DEL USUARIO
                // Tu StorageHelper devuelve String?, así que usamos el operador elvis (?:)
                // para poner un valor por defecto si es nulo.
                val nombreUsuario = storageHelper.getUserName() ?: "Usuario Anónimo"

                val nuevoReporte = DenunciaRequest(
                    nombreDenunciante = nombreUsuario, // <--- AQUÍ SE ENVÍA EL NOMBRE REAL
                    latitud = latLng.latitude,
                    longitud = latLng.longitude,
                    tipo = tipo,
                    descripcion = descripcion
                )

                // 2. Enviar el reporte al Backend
                RetrofitClient.instance.crearDenuncia(nuevoReporte)

                // 3. Éxito
                Toast.makeText(this@MainMapActivity, "✅ Reporte enviado por $nombreUsuario", Toast.LENGTH_SHORT).show()

                // Recargar mapa para ver el nuevo pin con la info actualizada
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