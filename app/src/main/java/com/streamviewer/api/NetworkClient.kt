package com.streamviewer.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private var retrofit: Retrofit? = null
    private var api: StreamViewerApi? = null

    // TODO use EncryptedSharedPreferences or DataStore instead of Memory
    var baseUrl: String = "http://192.168.0.177:5000" // Default, user can change
    var username: String? = null
    var password: String? = null
    var xtreamUrl: String? = null

    fun getApi(): StreamViewerApi {
        if (api == null) {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .addInterceptor(AuthInterceptor())
                .build()

            val safeBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            retrofit = Retrofit.Builder()
                .baseUrl(safeBaseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            api = retrofit!!.create(StreamViewerApi::class.java)
        }
        return api!!
    }

    fun resetApi() {
        api = null
        retrofit = null
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val original = chain.request()
            val builder = original.newBuilder()

            // Add headers if available
            if (!username.isNullOrEmpty() && !password.isNullOrEmpty() && !xtreamUrl.isNullOrEmpty()) {
                builder.header("X-Stream-User", username!!)
                builder.header("X-Stream-Pass", password!!)
                builder.header("X-Stream-Url", xtreamUrl!!)
            }

            return chain.proceed(builder.build())
        }
    }
}
