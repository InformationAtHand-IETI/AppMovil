package edu.eci.co.informationathand

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetalleReporteActivity : AppCompatActivity() {

    private lateinit var txtTitulo: TextView
    private lateinit var txtDetalle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_reporte)

        // Inicializar vistas
        txtTitulo = findViewById(R.id.txtTitulo)
        txtDetalle = findViewById(R.id.txtDetalle)

        // Obtener datos del intent
        val titulo = intent.getStringExtra("titulo") ?: "Sin título"
        val detalle = intent.getStringExtra("detalle") ?: "Sin detalles"

        // Mostrar información
        txtTitulo.text = titulo
        txtDetalle.text = detalle

        // Habilitar botón de retroceso en ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalle del Reporte"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}