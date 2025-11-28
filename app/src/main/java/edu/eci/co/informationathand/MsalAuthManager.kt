package edu.eci.co.informationathand

import android.app.Activity
import android.content.Context
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException

class MsalAuthManager(
    private val context: Context,
    private val configResId: Int
) {

    private var singleAccountApp: ISingleAccountPublicClientApplication? = null
    private val scopes = arrayOf("api://0300e252-00d2-41b3-be40-b72268b73d60/access_as_user")

    fun initialize(
        onReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            configResId,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {

                override fun onCreated(app: ISingleAccountPublicClientApplication) {
                    singleAccountApp = app
                    onReady()
                }

                override fun onError(exception: MsalException) {
                    onError(exception)
                }
            }
        )
    }

    fun loadAccount(
        onLoggedIn: (IAccount) -> Unit,
        onNoAccount: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        singleAccountApp?.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {

            override fun onAccountLoaded(activeAccount: IAccount?) {
                if (activeAccount != null) onLoggedIn(activeAccount)
                else onNoAccount()
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {}

            override fun onError(exception: MsalException) {
                onError(exception)
            }
        }) ?: onError(IllegalStateException("MSAL not initialized"))
    }

    fun signIn(
        activity: Activity,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (Exception) -> Unit,
        onCancel: () -> Unit
    ) {
        val app = singleAccountApp ?: return onError(IllegalStateException("MSAL not initialized"))

        app.signIn(activity, null, scopes, object : AuthenticationCallback {

            override fun onSuccess(result: IAuthenticationResult) {
                onSuccess(result)
            }

            override fun onError(exception: MsalException) {
                onError(exception)
            }

            override fun onCancel() {
                onCancel()
            }
        })
    }

    fun getJwt(
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val app = singleAccountApp
            ?: return onError(IllegalStateException("MSAL not initialized"))

        val account = app.currentAccount?.currentAccount
            ?: return onError(IllegalStateException("No active account"))

        app.acquireTokenSilentAsync(
            scopes,
            app.currentAccount?.currentAccount?.authority ?: "",
            object : SilentAuthenticationCallback {

                override fun onSuccess(result: IAuthenticationResult) {
                    val jwt = result.accessToken   // <--- Aquí está el JWT
                    onSuccess(jwt)
                }

                override fun onError(exception: MsalException) {
                    onError(exception)
                }
            }
        )
    }

}
