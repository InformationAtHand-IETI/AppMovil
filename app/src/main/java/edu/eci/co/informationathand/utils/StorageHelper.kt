package edu.eci.co.informationathand.utils

import android.content.Context
import android.content.SharedPreferences

class StorageHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AppMovilPrefs", Context.MODE_PRIVATE)

    // Guardar datos de usuario
    fun saveUserData(name: String, email: String) {
        sharedPreferences.edit().apply {
            putString("user_name", name)
            putString("user_email", email)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    // Obtener nombre de usuario
    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    // Obtener email de usuario
    fun getUserEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    // Verificar si el usuario está logueado
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    // Guardar reporte
    fun saveReport(latitude: Double, longitude: Double, title: String, description: String) {
        val reportId = System.currentTimeMillis().toString()
        sharedPreferences.edit().apply {
            putString("report_${reportId}_lat", latitude.toString())
            putString("report_${reportId}_lng", longitude.toString())
            putString("report_${reportId}_title", title)
            putString("report_${reportId}_desc", description)
            putLong("report_${reportId}_time", System.currentTimeMillis())

            // Guardar ID del reporte en lista
            val reportsList = getReportsList().toMutableSet()
            reportsList.add(reportId)
            putStringSet("reports_list", reportsList)

            apply()
        }
    }

    // Obtener lista de IDs de reportes
    fun getReportsList(): Set<String> {
        return sharedPreferences.getStringSet("reports_list", emptySet()) ?: emptySet()
    }

    // Obtener un reporte específico
    fun getReport(reportId: String): Report? {
        val lat = sharedPreferences.getString("report_${reportId}_lat", null)?.toDoubleOrNull()
        val lng = sharedPreferences.getString("report_${reportId}_lng", null)?.toDoubleOrNull()
        val title = sharedPreferences.getString("report_${reportId}_title", null)
        val desc = sharedPreferences.getString("report_${reportId}_desc", null)
        val time = sharedPreferences.getLong("report_${reportId}_time", 0)

        return if (lat != null && lng != null && title != null && desc != null) {
            Report(reportId, lat, lng, title, desc, time)
        } else {
            null
        }
    }

    // Obtener todos los reportes
    fun getAllReports(): List<Report> {
        val reports = mutableListOf<Report>()
        val reportIds = getReportsList()

        reportIds.forEach { reportId ->
            getReport(reportId)?.let { report ->
                reports.add(report)
            }
        }

        return reports.sortedByDescending { it.timestamp }
    }

    // Cerrar sesión
    fun logout() {
        sharedPreferences.edit().apply {
            putBoolean("is_logged_in", false)
            apply()
        }
    }

    // Limpiar todos los datos
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}

// Data class para representar un reporte
data class Report(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val description: String,
    val timestamp: Long
)