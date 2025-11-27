package edu.eci.co.informationathand

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleReporteActivity : AppCompatActivity() {

    // Referencias a la UI
    private lateinit var txtTipo: TextView
    private lateinit var txtEstado: TextView
    private lateinit var txtConteo: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtAutor: TextView
    private lateinit var txtDescripciones: TextView
    private lateinit var btnVolver: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_reporte)

        // Habilitar botón atrás en ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle del Incidente"

        // 1. Inicializar Vistas
        txtTipo = findViewById(R.id.txtTipoIncidente)
        txtEstado = findViewById(R.id.txtEstado)
        txtConteo = findViewById(R.id.txtConteo)
        txtFecha = findViewById(R.id.txtFecha)
        txtAutor = findViewById(R.id.txtAutor)
        txtDescripciones = findViewById(R.id.txtDescripciones)
        btnVolver = findViewById(R.id.btnVolver)

        // 2. Obtener datos del Intent (enviados desde MainMapActivity)
        val tipo = intent.getStringExtra("tipo") ?: "DESCONOCIDO"
        val estado = intent.getStringExtra("estado") ?: "ACTIVA"
        val conteo = intent.getIntExtra("conteo", 1)
        val fecha = intent.getStringExtra("fecha") ?: "Sin fecha"
        val autor = intent.getStringExtra("autor") ?: "Anónimo"
        val descripciones = intent.getStringExtra("descripciones") ?: "Sin detalles disponibles."

        // 3. Asignar valores a la vista
        txtTipo.text = tipo
        txtEstado.text = "Estado: $estado"
        txtConteo.text = "Reportes agrupados: $conteo"
        txtFecha.text = fecha
        txtAutor.text = autor
        txtDescripciones.text = descripciones

        // 4. Cambiar color del encabezado según el tipo (Coincide con tus ENUMs)
        colorearEncabezado(tipo)

        // 5. Acción del botón volver
        btnVolver.setOnClickListener {
            finish() // Cierra la actividad y vuelve al mapa
        }
    }

    private fun colorearEncabezado(tipo: String) {
        val color = when (tipo.uppercase()) {
            "GRAVE" -> Color.parseColor("#D32F2F") // Rojo
            "MEDIO", "CHOQUE" -> Color.parseColor("#F57C00") // Naranja
            "LEVE" -> Color.parseColor("#FBC02D") // Amarillo oscuro
            "PROTESTA" -> Color.parseColor("#7B1FA2") // Morado
            else -> Color.parseColor("#0288D1") // Azul (Mecánico/Varado/Default)
        }
        txtTipo.setBackgroundColor(color)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}