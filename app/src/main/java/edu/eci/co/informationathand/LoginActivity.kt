package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.eci.co.informationathand.utils.CognitoAuthHelper
import edu.eci.co.informationathand.utils.StorageHelper
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmailOrUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var storageHelper: StorageHelper
    private lateinit var cognitoAuth: CognitoAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        storageHelper = StorageHelper(this)
        cognitoAuth = CognitoAuthHelper()

        // Verificar sesión existente
        lifecycleScope.launch {
            if (cognitoAuth.isUserSignedIn()) {
                navigateToMap()
                return@launch
            }
        }

        // Inicializar vistas
        etEmailOrUsername = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            attemptLogin()
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun attemptLogin() {
        val emailOrUsername = etEmailOrUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validaciones básicas
        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que tenga al menos 3 caracteres (para username o email)
        if (emailOrUsername.length < 3) {
            Toast.makeText(this, "El usuario o email es muy corto", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Iniciar sesión con Cognito (acepta email o username)
        lifecycleScope.launch {
            val result = cognitoAuth.signIn(emailOrUsername, password)

            showLoading(false)

            result.onSuccess {
                // Obtener información del usuario
                val userInfoResult = cognitoAuth.getCurrentUser()
                userInfoResult.onSuccess { userInfo ->
                    val name = userInfo["name"] ?: "Usuario"
                    val email = userInfo["email"] ?: emailOrUsername
                    storageHelper.saveUserData(name, email)
                }

                Toast.makeText(this@LoginActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                navigateToMap()
            }.onFailure { error ->
                val errorMessage = when {
                    error.message?.contains("UserNotFoundException") == true ->
                        "Usuario o email no encontrado"
                    error.message?.contains("NotAuthorizedException") == true ->
                        "Contraseña incorrecta"
                    error.message?.contains("UserNotConfirmedException") == true ->
                        "Debes verificar tu email antes de iniciar sesión"
                    else -> "Error: ${error.message}"
                }

                Toast.makeText(
                    this@LoginActivity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !show
    }

    private fun navigateToMap() {
        val intent = Intent(this, MainMapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}