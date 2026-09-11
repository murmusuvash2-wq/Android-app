package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import java.io.File

/**
 * Clean data model representing the authenticated user's profile.
 * Supports displayName, email, photo URI reference, user identifier, and authentication provider.
 */
data class UserProfile(
    val userId: String = "usr_maya_01",
    val displayName: String = "Maya Sharma",
    val email: String = "maya.sharma@example.com",
    val profilePhotoUri: String? = null,
    val authProvider: String = "Google"
) {
    val initials: String
        get() {
            val parts = displayName.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return when {
                parts.isEmpty() -> "M"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            }
        }
}

/**
 * Single source of truth for the user's profile state.
 * Reactive via Compose mutableStateOf, with local SharedPreferences persistence support.
 */
object UserProfileRepository {
    private const val PREFS_NAME = "tihin_user_profile"
    private const val KEY_DISPLAY_NAME = "display_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHOTO_URI = "profile_photo_uri"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_AUTH_PROVIDER = "auth_provider"

    val DEFAULT_PROFILE = UserProfile(
        userId = "usr_maya_01",
        displayName = "Maya Sharma",
        email = "maya.sharma@example.com",
        profilePhotoUri = null,
        authProvider = "Google"
    )

    var profile by mutableStateOf(DEFAULT_PROFILE)
        private set

    /**
     * Initializes repository state from persistent storage if available.
     */
    fun init(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val name = prefs.getString(KEY_DISPLAY_NAME, DEFAULT_PROFILE.displayName) ?: DEFAULT_PROFILE.displayName
            val email = prefs.getString(KEY_EMAIL, DEFAULT_PROFILE.email) ?: DEFAULT_PROFILE.email
            val photoUri = prefs.getString(KEY_PHOTO_URI, DEFAULT_PROFILE.profilePhotoUri)
            val userId = prefs.getString(KEY_USER_ID, DEFAULT_PROFILE.userId) ?: DEFAULT_PROFILE.userId
            val authProvider = prefs.getString(KEY_AUTH_PROVIDER, DEFAULT_PROFILE.authProvider) ?: DEFAULT_PROFILE.authProvider

            profile = UserProfile(
                userId = userId,
                displayName = name,
                email = email,
                profilePhotoUri = photoUri,
                authProvider = authProvider
            )
        } catch (e: Exception) {
            // Safe fallback to default profile in case of storage issues
            profile = DEFAULT_PROFILE
        }
    }

    /**
     * Updates user profile attributes and persists them.
     */
    fun updateProfile(
        displayName: String,
        profilePhotoUri: String?,
        context: Context? = null
    ) {
        val trimmed = displayName.trim()
        val validName = if (trimmed.isNotEmpty()) trimmed else profile.displayName
        val safePhotoUri = if (context != null && !profilePhotoUri.isNullOrBlank() && !profilePhotoUri.startsWith("http")) {
            persistProfileImageToInternal(context, profilePhotoUri)
        } else {
            profilePhotoUri
        }

        profile = profile.copy(
            displayName = validName,
            profilePhotoUri = safePhotoUri
        )

        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(KEY_DISPLAY_NAME, validName)
                    .putString(KEY_PHOTO_URI, safePhotoUri)
                    .putString(KEY_EMAIL, profile.email)
                    .putString(KEY_USER_ID, profile.userId)
                    .putString(KEY_AUTH_PROVIDER, profile.authProvider)
                    .apply()
            } catch (e: Exception) {
                // Ignore storage issues in test/restricted environments
            }
        }
    }

    private fun persistProfileImageToInternal(context: Context, sourceUriString: String): String {
        return try {
            val uri = Uri.parse(sourceUriString)
            val dir = File(context.filesDir, "profile_photos").apply { mkdirs() }
            val destFile = File(dir, "profile_avatar.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            sourceUriString
        }
    }

    /**
     * Resets repository state to default initial conditions.
     * Useful for isolated testing.
     */
    fun resetForTesting(
        customProfile: UserProfile = DEFAULT_PROFILE,
        context: Context? = null
    ) {
        profile = customProfile
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Creates a temporary file Uri for camera capture using the app's FileProvider.
     */
    fun createTempProfileCameraUri(context: Context): Uri? {
        return try {
            val photosDir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
            val tempFile = File.createTempFile("profile_", ".jpg", photosDir)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            null
        }
    }
}
