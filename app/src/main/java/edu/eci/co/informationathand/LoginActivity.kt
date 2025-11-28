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
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import edu.eci.co.informationathand.utils.StorageHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvRegister: TextView

    private lateinit var mSingleAccountApp: ISingleAccountPublicClientApplication
    private lateinit var storageHelper: StorageHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        storageHelper = StorageHelper(this)

        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvRegister = findViewById(R.id.tvRegister)


        PublicClientApplication.createSingleAccountPublicClientApplication(
            this,
            R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {

                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mSingleAccountApp = application
                    loadAccount()
                }

                override fun onError(exception: MsalException?) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Error al inicializar MSAL: ${exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )

        btnLogin.setOnClickListener {
            signIn()
        }

        tvRegister.setOnClickListener {
            Toast.makeText(this, "Registro no aplica con MSAL", Toast.LENGTH_SHORT).show()
        }
    }

    // -----------------------------------------
    //   Cargar la cuenta si ya hay sesión
    // -----------------------------------------
    private fun loadAccount() {
        mSingleAccountApp.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {

            override fun onAccountLoaded(activeAccount: IAccount?) {
                if (activeAccount != null) {
                    val name = activeAccount.username ?: "Usuario"
                    val email = activeAccount.claims?.get("preferred_username")?.toString() ?: name

                    storageHelper.saveUserData(name, email)
                    navigateToMap()
                }
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {}

            override fun onError(exception: MsalException) {
                Toast.makeText(this@LoginActivity, "Error: ${exception?.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // -----------------------------------------
    //   Sign-in interactivo con MSAL
    // -----------------------------------------
    private fun signIn() {
        showLoading(true)

        mSingleAccountApp.signIn(
            this,
            null,
            arrayOf("api://0300e252-00d2-41b3-be40-b72268b73d60/access_as_user"),
            object : AuthenticationCallback {

                override fun onSuccess(result: IAuthenticationResult) {
                    val account = result.account

                    val name = account.username
                    val email = result.account.claims?.get("preferred_username")?.toString() ?: name

                    storageHelper.saveUserData(name, email)

                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    navigateToMap()
                }

                override fun onError(exception: MsalException) {
                    showLoading(false)
                    println(exception.message)
                    Toast.makeText(
                        this@LoginActivity,
                        "Error: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCancel() {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Inicio cancelado", Toast.LENGTH_SHORT).show()
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
