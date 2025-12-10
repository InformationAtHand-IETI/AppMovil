package edu.eci.co.informationathand

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import edu.eci.co.informationathand.utils.StorageHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    private var msalApp: ISingleAccountPublicClientApplication? = null
    private lateinit var storageHelper: StorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        storageHelper = StorageHelper(this)

        btnLogin = findViewById(R.id.btnLogin)
        progressBar = ProgressBar(this).apply { visibility = View.GONE }

        // Deshabilitar botón mientras carga MSAL
        btnLogin.isEnabled = false
        btnLogin.text = "Cargando..."

        // Inicializar MSAL con tu archivo msal_config.json
        PublicClientApplication.createSingleAccountPublicClientApplication(
            this,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {

                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    msalApp = application
                    btnLogin.isEnabled = true
                    btnLogin.text = "Iniciar Sesión"
                    loadAccount()
                }

                override fun onError(exception: MsalException?) {
                    Log.e("MSAL", "Error init", exception)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error cargando MSAL: ${exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        btnLogin.setOnClickListener {
            if (msalApp == null) {
                Toast.makeText(this, "MSAL aún está inicializando...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signIn()
        }
    }

    private fun loadAccount() {
        msalApp?.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {

            override fun onAccountLoaded(activeAccount: IAccount?) {
                if (activeAccount != null) {
                    val name = activeAccount.claims?.get("name")?.toString()
                        ?: activeAccount.username
                    val email = activeAccount.username

                    storageHelper.saveUserData(name, email)
                    navigateToMap()
                }
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {}

            override fun onError(exception: MsalException) {
                Log.e("MSAL", "loadAccount error", exception)
            }
        })
    }

    private fun signIn() {
        showLoading(true)

        msalApp?.signIn(
            this,
            null,
            arrayOf("api://0300e252-00d2-41b3-be40-b72268b73d60/access_as_user"),
            object : AuthenticationCallback {

                override fun onSuccess(result: IAuthenticationResult) {
                    val account = result.account

                    val name = account.claims?.get("name")?.toString()
                        ?: account.username
                    val email = account.username

                    storageHelper.saveUserData(name, email)

                    Toast.makeText(this@LoginActivity, "Bienvenido, $name", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    navigateToMap()
                }

                override fun onError(exception: MsalException) {
                    showLoading(false)
                    println(exception.message)
                    Log.e("MSAL", "Login error", exception)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error iniciando sesión: ${exception.message}",
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
        btnLogin.isEnabled = !show
        btnLogin.alpha = if (show) 0.5f else 1f
    }

    private fun navigateToMap() {
        val intent = Intent(this, MainMapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
