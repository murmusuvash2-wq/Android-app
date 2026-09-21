package com.example.data.datasource

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseConfig {

    private const val FALLBACK_SUPABASE_URL = "eopcqvnqkkfwkviymkue.supabase.co"
    private const val FALLBACK_ANON_KEY = "sb_publishable_eopcqvnqkkfwkviymkue_placeholder"

    val supabaseUrl: String
        get() {
            val configured = BuildConfig.SUPABASE_URL
            val raw = if (configured.isNotBlank() && configured != "placeholder") {
                configured
            } else {
                FALLBACK_SUPABASE_URL
            }
            return if (raw.startsWith("http://") || raw.startsWith("https://")) {
                if (raw.endsWith("/")) raw else "$raw/"
            } else {
                "https://$raw/"
            }
        }

    val anonKey: String
        get() {
            val configured = BuildConfig.SUPABASE_ANON_KEY
            return if (configured.isNotBlank() && configured != "placeholder") {
                configured
            } else {
                // If not set in BuildConfig, fallback to system environment or default
                System.getenv("SUPABASE_ANON_KEY") ?: FALLBACK_ANON_KEY
            }
        }

    val isConfigured: Boolean
        get() {
            val url = supabaseUrl
            val key = anonKey
            return url.isNotBlank() && key.isNotBlank() && !key.contains("placeholder")
        }

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(supabaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val productApi: SupabaseProductApi by lazy {
        retrofit.create(SupabaseProductApi::class.java)
    }

    val authApi: SupabaseAuthApi by lazy {
        retrofit.create(SupabaseAuthApi::class.java)
    }

    val storageApi: SupabaseStorageApi by lazy {
        retrofit.create(SupabaseStorageApi::class.java)
    }

    val functionsApi: SupabaseFunctionsApi by lazy {
        retrofit.create(SupabaseFunctionsApi::class.java)
    }
}
