package edu.eci.co.informationathand.utils

import android.util.Log
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CognitoAuthHelper {

    // Registro de usuario con username personalizado
    suspend fun signUp(username: String, name: String, email: String, password: String): Result<String> {
        return suspendCancellableCoroutine { continuation ->

            // Crear el atributo email manualmente
            val emailAttribute = com.amplifyframework.auth.AuthUserAttribute(
                AuthUserAttributeKey.custom("email"),
                email
            )

            val nameAttribute = com.amplifyframework.auth.AuthUserAttribute(
                AuthUserAttributeKey.name(),
                name
            )

            val options = AuthSignUpOptions.builder()
                .userAttributes(listOf(emailAttribute, nameAttribute))
                .build()

            Log.i("CognitoAuth", "Intentando registrar usuario: $username con email: $email")

            Amplify.Auth.signUp(username, password, options,
                { result ->
                    Log.i("CognitoAuth", "Registro exitoso para usuario: $username")
                    continuation.resume(Result.success("Usuario registrado exitosamente"))
                },
                { error ->
                    Log.e("CognitoAuth", "Error en registro: ${error.message}", error)
                    continuation.resume(Result.failure(error))
                }
            )
        }
    }

    // Confirmar código de verificación usando el username
    suspend fun confirmSignUp(username: String, code: String): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            Amplify.Auth.confirmSignUp(username, code,
                { result ->
                    Log.i("CognitoAuth", "Verificación exitosa")
                    continuation.resume(Result.success("Cuenta verificada"))
                },
                { error ->
                    Log.e("CognitoAuth", "Error en verificación", error)
                    continuation.resume(Result.failure(error))
                }
            )
        }
    }

    // Inicio de sesión (puede usar email o username gracias al alias)
    suspend fun signIn(emailOrUsername: String, password: String): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            Amplify.Auth.signIn(emailOrUsername, password,
                { result ->
                    if (result.isSignedIn) {
                        Log.i("CognitoAuth", "Inicio de sesión exitoso")
                        continuation.resume(Result.success("Inicio de sesión exitoso"))
                    } else {
                        continuation.resume(Result.failure(Exception("Inicio de sesión incompleto")))
                    }
                },
                { error ->
                    Log.e("CognitoAuth", "Error en inicio de sesión", error)
                    continuation.resume(Result.failure(error))
                }
            )
        }
    }

    // Cerrar sesión
    suspend fun signOut(): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            Amplify.Auth.signOut(
                {
                    Log.i("CognitoAuth", "Sesión cerrada")
                    continuation.resume(Result.success("Sesión cerrada"))
                } as AuthSignOutOptions,
                { error ->
                    Log.e("CognitoAuth", "Error al cerrar sesión")

                }
            )
        }
    }

    // Verificar si hay sesión activa
    suspend fun isUserSignedIn(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            Amplify.Auth.fetchAuthSession(
                { session ->
                    continuation.resume(session.isSignedIn)
                },
                { error ->
                    Log.e("CognitoAuth", "Error al verificar sesión", error)
                    continuation.resume(false)
                }
            )
        }
    }

    // Obtener información del usuario actual
    suspend fun getCurrentUser(): Result<Map<String, String>> {
        return suspendCancellableCoroutine { continuation ->
            Amplify.Auth.fetchUserAttributes(
                { attributes ->
                    val userInfo = mutableMapOf<String, String>()
                    attributes.forEach { attr ->
                        userInfo[attr.key.keyString] = attr.value
                    }
                    continuation.resume(Result.success(userInfo))
                },
                { error ->
                    Log.e("CognitoAuth", "Error al obtener usuario", error)
                    continuation.resume(Result.failure(error))
                }
            )
        }
    }
}