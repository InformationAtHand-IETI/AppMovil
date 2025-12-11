package edu.eci.co.informationathand.chat.network

import com.google.gson.GsonBuilder
import edu.eci.co.informationathand.AuthInterceptor
import edu.eci.co.informationathand.MsalAuthManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://parchesecgateway.azure-api.net/api/"

    fun create(authManager: MsalAuthManager): ChatApiService {

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(ChatApiService::class.java)
    }

}
