package edu.eci.co.informationathand

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authManager: MsalAuthManager
) : Interceptor {

    @Volatile
    private var cachedToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getTokenBlocking()

        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }

    private fun getTokenBlocking(): String {
        cachedToken?.let { return it }

        var result: String? = null
        var error: Exception? = null

        val latch = java.util.concurrent.CountDownLatch(1)

        authManager.getJwt(
            onSuccess = {
                cachedToken = it
                result = it
                latch.countDown()
            },
            onError = {
                error = it
                latch.countDown()
            }
        )

        latch.await()

        if (error != null)
            throw error!!

        return result!!
    }
}
