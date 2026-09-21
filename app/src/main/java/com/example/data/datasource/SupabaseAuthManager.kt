package com.example.data.datasource

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object SupabaseAuthManager {

    private const val TAG = "SupabaseAuthManager"
    private const val PREFS_NAME = "tihin_supabase_auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"

    // Default primary test credentials
    private const val DEFAULT_USER_ID = "47c46ecc-4f8d-494f-b68c-b3d835d120b0"
    private const val DEFAULT_EMAIL = "suvash.astrology@gmail.com"
    private const val DEFAULT_PASSWORD = "Password123!"

    private var prefs: SharedPreferences? = null
    private val authMutex = Mutex()

    @Volatile
    private var cachedToken: String? = null
    @Volatile
    private var cachedUserId: String? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            cachedToken = prefs?.getString(KEY_ACCESS_TOKEN, null)
            cachedUserId = prefs?.getString(KEY_USER_ID, DEFAULT_USER_ID)
        }
    }

    fun getUserId(): String {
        return cachedUserId ?: prefs?.getString(KEY_USER_ID, DEFAULT_USER_ID) ?: DEFAULT_USER_ID
    }

    fun getCachedAccessToken(): String? {
        return cachedToken ?: prefs?.getString(KEY_ACCESS_TOKEN, null)
    }

    suspend fun ensureAuthenticatedSession(
        email: String = DEFAULT_EMAIL,
        password: String = DEFAULT_PASSWORD
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        authMutex.withLock {
            val existingToken = getCachedAccessToken()
            val existingUserId = getUserId()
            if (!existingToken.isNullOrBlank() && existingUserId.isNotBlank()) {
                return@withContext Result.success(Pair(existingUserId, existingToken))
            }

            try {
                Log.d(TAG, "Requesting Supabase session for $email")
                val response = SupabaseConfig.authApi.signInWithPassword(
                    apiKey = SupabaseConfig.anonKey,
                    grantType = "password",
                    request = SupabaseAuthRequest(email = email, password = password)
                )

                val token = response.accessToken
                val userId = response.user.id

                cachedToken = token
                cachedUserId = userId

                prefs?.edit()?.apply {
                    putString(KEY_ACCESS_TOKEN, token)
                    putString(KEY_USER_ID, userId)
                    putString(KEY_USER_EMAIL, email)
                    apply()
                }

                Log.d(TAG, "Supabase session established. User: $userId")
                Result.success(Pair(userId, token))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to authenticate with Supabase: ${e.message}", e)
                // If network fails but we had a fallback userId, report failure or fallback
                Result.failure(e)
            }
        }
    }

    fun saveSession(userId: String, token: String, email: String? = null) {
        cachedUserId = userId
        cachedToken = token
        prefs?.edit()?.apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_ACCESS_TOKEN, token)
            if (email != null) putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun clearSession() {
        cachedUserId = null
        cachedToken = null
        prefs?.edit()?.clear()?.apply()
    }
}
