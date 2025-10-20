package edu.eci.co.informationathand

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class DetalleReporteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_reporte)

        val titulo = intent.getStringExtra("titulo")
        val detalle = intent.getStringExtra("detalle")

        val txtTitulo = findViewById<TextView>(R.id.txtTitulo)
        val txtDetalle = findViewById<TextView>(R.id.txtDetalle)

        txtTitulo.text = titulo
        txtDetalle.text = detalle
    }
}
