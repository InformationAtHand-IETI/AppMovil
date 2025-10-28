package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.eci.co.informationathand.utils.StorageHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var storageHelper: StorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 🔹 Inicializar StorageHelper
        storageHelper = StorageHelper(this)

        // 🔹 Si el usuario ya está logueado, ir directamente al mapa
        if (storageHelper.isLoggedIn()) {
            navigateToMap()
            return
        }

        // 🔹 Inicializar vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)

        // 🔹 Botón para iniciar sesión
        btnLogin.setOnClickListener {
            attemptLogin()
        }

        // 🔹 Ir a la pantalla de registro
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Mostrar progreso
        progressBar.visibility = ProgressBar.VISIBLE
        btnLogin.isEnabled = false

        // 🔹 Simular inicio de sesión
        simulateLogin(email)
    }

    private fun simulateLogin(email: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = ProgressBar.GONE
            btnLogin.isEnabled = true

            // 🔹 Guardar sesión del usuario
            storageHelper.saveUserData("Usuario", email)

            Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
            navigateToMap()
        }, 2000)
    }

    private fun navigateToMap() {
        val intent = Intent(this, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
