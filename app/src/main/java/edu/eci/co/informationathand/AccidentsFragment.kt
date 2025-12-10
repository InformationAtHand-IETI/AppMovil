package edu.eci.co.informationathand

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class AccidentsFragment : Fragment() {

    private lateinit var container: LinearLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asegúrate de que este layout tenga un ScrollView conteniendo al LinearLayout
        return inflater.inflate(R.layout.fragment_accidents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container = view.findViewById(R.id.accidentsContainer)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 1. Intentamos obtener la ubicación para ordenar
        obtenerUbicacionYCargarDatos()
    }

    private fun obtenerUbicacionYCargarDatos() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Si no hay permiso, cargamos los datos sin ordenar por distancia (o usando una referencia base)
            cargarDatosDesdeBackend(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            // 'location' puede ser null si el GPS está apagado o no ha fixado
            cargarDatosDesdeBackend(location)
        }
    }

    private fun cargarDatosDesdeBackend(miUbicacion: Location?) {
        lifecycleScope.launch {
            try {
                // 2. Traer TODOS los reportes de Azure
                val listaDenuncias = RetrofitClient.instance.obtenerDenuncias()

                if (listaDenuncias.isEmpty()) {
                    showEmptyState(container)
                } else {
                    // 3. LOGICA DE ORDENAMIENTO (Magia aquí) 🪄
                    val listaOrdenada = if (miUbicacion != null) {
                        listaDenuncias.sortedBy { denuncia ->
                            calcularDistancia(miUbicacion.latitude, miUbicacion.longitude, denuncia.latitud, denuncia.longitud)
                        }
                    } else {
                        listaDenuncias // Si no hay GPS, se muestran como llegan (por fecha usualmente)
                    }

                    // Limpiar contenedor antes de agregar
                    container.removeAllViews()

                    // 4. Pintar las tarjetas
                    listaOrdenada.forEach { denuncia ->
                        val card = createReportCard(denuncia, miUbicacion)
                        container.addView(card)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error cargando reportes", Toast.LENGTH_SHORT).show()
                showEmptyState(container)
            }
        }
    }

    // Función auxiliar matemática para calcular metros
    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] // Retorna metros
    }

    private fun createReportCard(denuncia: DenunciaResponse, miUbicacion: Location?): View {
        val cardView = layoutInflater.inflate(R.layout.item_accident_card, null) as CardView

        val tvTitle = cardView.findViewById<TextView>(R.id.tvReportTitle)
        val tvDescription = cardView.findViewById<TextView>(R.id.tvReportDescription)
        val tvDate = cardView.findViewById<TextView>(R.id.tvReportDate)
        val btnViewLocation = cardView.findViewById<TextView>(R.id.btnViewLocation) // Asumo que es un TextView o Button

        // Configurar Textos
        // Si hay ubicación, mostramos la distancia en el título o descripción
        if (miUbicacion != null) {
            val metros = calcularDistancia(miUbicacion.latitude, miUbicacion.longitude, denuncia.latitud, denuncia.longitud)
            val distanciaTexto = if (metros > 1000) {
                String.format("%.1f km", metros / 1000)
            } else {
                "${metros.toInt()} m"
            }
            tvTitle.text = "${denuncia.tipo} (a $distanciaTexto)"
        } else {
            tvTitle.text = denuncia.tipo
        }

        // Mostrar la primera descripción si existe
        val descTexto = if (denuncia.descripciones.isNotEmpty()) denuncia.descripciones[0] else "Sin detalles"
        tvDescription.text = descTexto

        // Formatear fecha (Azure la manda como String ISO, ej: 2025-11-26T...)
        val fechaBonita = denuncia.fechaCreacion?.replace("T", " ")?.substring(0, 16) ?: ""
        tvDate.text = fechaBonita

        // --- CLIC EN LA TARJETA (Ver Detalle) ---
        cardView.setOnClickListener {
            val intent = Intent(requireContext(), DetalleReporteActivity::class.java)
            intent.putExtra("tipo", denuncia.tipo)
            intent.putExtra("estado", denuncia.estado)
            intent.putExtra("conteo", denuncia.conteo)
            intent.putExtra("descripciones", denuncia.descripciones.joinToString("\n"))
            intent.putExtra("fecha", denuncia.fechaCreacion)
            intent.putExtra("autor", denuncia.nombreDenunciante)
            startActivity(intent)
        }

        // --- CLIC EN "VER UBICACIÓN" (Ir al Mapa) ---
        btnViewLocation.setOnClickListener {
            navigateToMapLocation(denuncia.latitud, denuncia.longitud, denuncia.tipo)
        }

        // Añadir margen inferior a la tarjeta para que no se peguen
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 32)
        cardView.layoutParams = params

        return cardView
    }

    private fun navigateToMapLocation(latitude: Double, longitude: Double, title: String) {
        val intent = Intent(requireContext(), MainMapActivity::class.java).apply {
            putExtra("center_latitude", latitude)
            putExtra("center_longitude", longitude)
            putExtra("marker_title", title)
            putExtra("zoom_to_marker", true)
            // Banderas para limpiar el stack si es necesario, aunque startActivity por defecto está bien aquí
        }
        startActivity(intent)
    }

    private fun showEmptyState(container: LinearLayout) {
        container.removeAllViews() // Limpiar por si acaso

        val emptyLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 128, 32, 64)
        }

        val emptyText = TextView(requireContext()).apply {
            text = "No hay reportes cerca"
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#999999"))
            gravity = android.view.Gravity.CENTER
        }

        emptyLayout.addView(emptyText)
        container.addView(emptyLayout)
    }
}