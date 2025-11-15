package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import edu.eci.co.informationathand.utils.StorageHelper
import java.text.SimpleDateFormat
import java.util.*

class AccidentsFragment : Fragment() {

    private lateinit var storageHelper: StorageHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_accidents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageHelper = StorageHelper(requireContext())
        val container = view.findViewById<LinearLayout>(R.id.accidentsContainer)

        // Cargar reportes
        val reports = storageHelper.getAllReports()

        if (reports.isEmpty()) {
            showEmptyState(container)
        } else {
            reports.forEach { report ->
                val card = createReportCard(report)
                container.addView(card)
            }
        }
    }

    private fun showEmptyState(container: LinearLayout) {
        val emptyLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
        }

        val emptyIcon = android.widget.ImageView(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_report_image)
            setColorFilter(android.graphics.Color.parseColor("#CCCCCC"))
            layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                bottomMargin = 16
            }
        }

        val emptyText = TextView(requireContext()).apply {
            text = "No hay reportes disponibles"
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#999999"))
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8
            }
        }

        val emptySubtext = TextView(requireContext()).apply {
            text = "Los reportes de accidentes aparecerán aquí"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
            gravity = android.view.Gravity.CENTER
        }

        emptyLayout.addView(emptyIcon)
        emptyLayout.addView(emptyText)
        emptyLayout.addView(emptySubtext)
        container.addView(emptyLayout)
    }

    private fun createReportCard(report: edu.eci.co.informationathand.utils.Report): View {
        val cardView = layoutInflater.inflate(R.layout.item_accident_card, null) as CardView

        val tvTitle = cardView.findViewById<TextView>(R.id.tvReportTitle)
        val tvDescription = cardView.findViewById<TextView>(R.id.tvReportDescription)
        val tvDate = cardView.findViewById<TextView>(R.id.tvReportDate)
        val btnViewLocation = cardView.findViewById<TextView>(R.id.btnViewLocation)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        tvTitle.text = report.title
        tvDescription.text = report.description
        tvDate.text = sdf.format(Date(report.timestamp))

        // Click en toda la card: Ver detalles
        cardView.setOnClickListener {
            val intent = Intent(requireContext(), DetalleReporteActivity::class.java)
            intent.putExtra("titulo", report.title)
            intent.putExtra("detalle", "${report.description}\n\n📅 Fecha: ${sdf.format(Date(report.timestamp))}\n📍 Lat: ${report.latitude}, Lng: ${report.longitude}")
            intent.putExtra("latitude", report.latitude)
            intent.putExtra("longitude", report.longitude)
            startActivity(intent)
        }

        // Click en "Ver Ubicación": Navegar al mapa
        btnViewLocation.setOnClickListener {
            navigateToMapLocation(report.latitude, report.longitude, report.title)
        }

        return cardView
    }

    private fun navigateToMapLocation(latitude: Double, longitude: Double, title: String) {
        // Opción 1: Navegar a MainMapActivity y centrar el mapa
        val intent = Intent(requireContext(), MainMapActivity::class.java).apply {
            putExtra("center_latitude", latitude)
            putExtra("center_longitude", longitude)
            putExtra("marker_title", title)
            putExtra("zoom_to_marker", true)
        }
        startActivity(intent)
    }
}