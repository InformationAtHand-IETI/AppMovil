package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import edu.eci.co.informationathand.utils.StorageHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvRegister: TextView

    // Hacemos esta variable nullable (?) para evitar el crash de "lateinit property not initialized"
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private lateinit var storageHelper: StorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        storageHelper = StorageHelper(this)

        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvRegister = findViewById(R.id.tvRegister)

        // 1. Deshabilitar botón hasta que MSAL cargue para evitar CRASH
        btnLogin.isEnabled = false
        btnLogin.text = "Cargando..."

        PublicClientApplication.createSingleAccountPublicClientApplication(
            this,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {

                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    // 2. MSAL Cargó: Habilitamos la variable y el botón
                    mSingleAccountApp = application
                    btnLogin.isEnabled = true
                    btnLogin.text = "Iniciar Sesión"
                    loadAccount()
                }

                override fun onError(exception: MsalException?) {
                    Log.e("MSAL", "Error init", exception)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error config: ${exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        btnLogin.setOnClickListener {
            if (mSingleAccountApp == null) {
                Toast.makeText(this, "MSAL no está listo aún", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signIn()
        }

        tvRegister.setOnClickListener {
            Toast.makeText(this, "Registro gestionado por Microsoft", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAccount() {
        if (mSingleAccountApp == null) return

        mSingleAccountApp?.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {

            override fun onAccountLoaded(activeAccount: IAccount?) {
                if (activeAccount != null) {
                    // Lógica corregida para obtener nombre
                    val name = activeAccount.claims?.get("name")?.toString() ?: activeAccount.username
                    val email = activeAccount.username // El username suele ser el email/UPN

                    storageHelper.saveUserData(name, email)
                    navigateToMap()
                }
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {}

            override fun onError(exception: MsalException) {
                // Error silencioso en carga automática
            }
        })
    }

    private fun signIn() {
        showLoading(true)

        mSingleAccountApp?.signIn(
            this,
            null,
            arrayOf("User.Read"),
            object : AuthenticationCallback {

                override fun onSuccess(result: IAuthenticationResult) {
                    val account = result.account

                    // Lógica corregida para obtener nombre
                    val name = account.claims?.get("name")?.toString() ?: account.username
                    val email = account.username

                    storageHelper.saveUserData(name, email)

                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Hola, $name", Toast.LENGTH_SHORT).show()
                    navigateToMap()
                }

                override fun onError(exception: MsalException) {
                    showLoading(false)
                    Log.e("MSAL", "Error Signin", exception)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error Login: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCancel() {
                    showLoading(false)
                }
            }
        )
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