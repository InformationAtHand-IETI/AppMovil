package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.eci.co.informationathand.utils.CognitoAuthHelper
import edu.eci.co.informationathand.utils.StorageHelper
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var storageHelper: StorageHelper
    private lateinit var cognitoAuth: CognitoAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        storageHelper = StorageHelper(this)
        cognitoAuth = CognitoAuthHelper()

        // Inicializar vistas
        etUsername = findViewById(R.id.etUsername)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)
        progressBar = findViewById(R.id.progressBar)

        btnRegister.setOnClickListener {
            registerUser()
        }

        tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validaciones
        if (username.isEmpty()) {
            etUsername.error = "Ingresa un nombre de usuario"
            etUsername.requestFocus()
            return
        }

        if (username.length < 3) {
            etUsername.error = "El username debe tener al menos 3 caracteres"
            etUsername.requestFocus()
            return
        }

        // Validar que el username no tenga formato de email
        if (username.contains("@")) {
            etUsername.error = "El username no puede ser un email"
            etUsername.requestFocus()
            return
        }

        if (name.isEmpty()) {
            etName.error = "Ingresa tu nombre"
            etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Ingresa tu email"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Email inválido"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa una contraseña"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return
        }

        // Mostrar progreso
        showLoading(true)

        // Registrar en Cognito
        lifecycleScope.launch {
            val result = cognitoAuth.signUp(
                username = username,
                name = name,
                email = email,
                password = password
            )

            showLoading(false)

            result.onSuccess {
                // Mostrar diálogo para código de verificación
                showVerificationDialog(username, email)
            }.onFailure { error ->
                Log.e("RegisterActivity", "Error en registro", error)

                val errorMessage = when {
                    error.message?.contains("UsernameExistsException") == true ->
                        "Este nombre de usuario ya está en uso"
                    error.message?.contains("InvalidPasswordException") == true ->
                        "La contraseña no cumple con los requisitos"
                    else -> "Error: ${error.message}"
                }

                Toast.makeText(
                    this@RegisterActivity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showVerificationDialog(username: String, email: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_verificaion, null)
        val etCode = dialogView.findViewById<EditText>(R.id.etVerificationCode)

        AlertDialog.Builder(this)
            .setTitle("Verificación de Email")
            .setMessage("Hemos enviado un código de verificación a $email")
            .setView(dialogView)
            .setPositiveButton("Verificar") { _, _ ->
                val code = etCode.text.toString().trim()
                if (code.isNotEmpty()) {
                    verifyCode(username, code)
                }
            }
            .setNegativeButton("Cancelar", null)
            .setCancelable(false)
            .show()
    }

    private fun verifyCode(username: String, code: String) {
        showLoading(true)

        lifecycleScope.launch {
            val result = cognitoAuth.confirmSignUp(username, code)

            showLoading(false)

            result.onSuccess {
                storageHelper.saveUserData(etName.text.toString(), etEmail.text.toString())
                Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }.onFailure { error ->
                Toast.makeText(
                    this@RegisterActivity,
                    "Error en verificación: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !show
    }
}