package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRegistro = findViewById<Button>(R.id.btn_registro)
        val btnMapa = findViewById<Button>(R.id.btn_mapa)
        val btnReportes = findViewById<Button>(R.id.btn_reportes)

        // Ir al registro
        btnRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Ir al mapa
        btnMapa.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

    }
}
