package edu.eci.co.informationathand

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("MyApplication", "Amplify inicializado correctamente")
        } catch (error: AmplifyException) {
            Log.e("MyApplication", "Error al inicializar Amplify", error)
        }
    }
}