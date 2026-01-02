package com.streamviewer.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private var retrofit: Retrofit? = null
    private var api: StreamViewerApi? = null
    private var sharedPreferences: SharedPreferences? = null

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_XTREAM_URL = "xtream_url"
    private const val DEFAULT_BASE_URL = "http://192.168.0.177:5000"

    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        sharedPreferences = EncryptedSharedPreferences.create(
            PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // TODO use EncryptedSharedPreferences or DataStore instead of Memory
    var baseUrl: String
        get() = sharedPreferences?.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) {
            sharedPreferences?.edit()?.putString(KEY_BASE_URL, value)?.apply()
        }

    var username: String?
        get() = sharedPreferences?.getString(KEY_USERNAME, null)
        set(value) {
            sharedPreferences?.edit()?.putString(KEY_USERNAME, value)?.apply()
        }

    var password: String?
        get() = sharedPreferences?.getString(KEY_PASSWORD, null)
        set(value) {
            sharedPreferences?.edit()?.putString(KEY_PASSWORD, value)?.apply()
        }

    var xtreamUrl: String?
        get() = sharedPreferences?.getString(KEY_XTREAM_URL, null)
        set(value) {
            sharedPreferences?.edit()?.putString(KEY_XTREAM_URL, value)?.apply()
        }

    fun getApi(): StreamViewerApi {
        if (api == null) {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .addInterceptor(AuthInterceptor())
                .build()

            val currentUrl = baseUrl
            val safeBaseUrl = if (currentUrl.endsWith("/")) currentUrl else "$currentUrl/"
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
            val user = username
            val pass = password
            val xtream = xtreamUrl

            if (!user.isNullOrEmpty() && !pass.isNullOrEmpty() && !xtream.isNullOrEmpty()) {
                builder.header("X-Stream-User", user)
                builder.header("X-Stream-Pass", pass)
                builder.header("X-Stream-Url", xtream)
            }

            return chain.proceed(builder.build())
        }
    }
}
