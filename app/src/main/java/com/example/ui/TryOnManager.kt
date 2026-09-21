package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import java.io.File

object TryOnManager {
    private const val PREFS_NAME = "tihin_tryon_prefs"
    private const val KEY_USER_PHOTO_URI = "user_photo_uri"

    var selectedProductId: String? by mutableStateOf(null)
    var selectedProductName: String? by mutableStateOf(null)
    var selectedProductBrand: String? by mutableStateOf(null)
    var selectedProductPrice: Double by mutableStateOf(0.0)
    var selectedProductImage: String? by mutableStateOf(null)

    var selectedUserPhotoUri: Uri? by mutableStateOf(null)
    var generatedResultImageUri: String? by mutableStateOf(null)

    var isProcessing: Boolean by mutableStateOf(false)
    var processingStatusMessage: String by mutableStateOf("")
    var lastRequestId: String? by mutableStateOf(null)
    var lastError: String? by mutableStateOf(null)
    var userPhotoStoragePath: String? by mutableStateOf(null)

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUri = prefs.getString(KEY_USER_PHOTO_URI, null)
        if (savedUri != null) {
            selectedUserPhotoUri = Uri.parse(savedUri)
        }
    }

    private fun saveState() {
        val context = appContext ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_PHOTO_URI, selectedUserPhotoUri?.toString()).apply()
    }

    fun updateUserPhoto(uriString: String, context: Context? = null) {
        selectedUserPhotoUri = Uri.parse(uriString)
        saveState()
    }

    fun createTempCameraUri(context: Context): Uri {
        return try {
            val imagePath = File(context.cacheDir, "camera_photos")
            if (!imagePath.exists()) imagePath.mkdirs()
            val newFile = File(imagePath, "temp_camera_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                newFile
            )
        } catch (_: Exception) {
            Uri.parse("content://${context.packageName}.fileprovider/camera_photos/temp.jpg")
        }
    }

    fun clear() {
        selectedProductId = null
        selectedProductName = null
        selectedProductBrand = null
        selectedProductPrice = 0.0
        selectedProductImage = null
        generatedResultImageUri = null
        isProcessing = false
        processingStatusMessage = ""
        lastRequestId = null
        lastError = null
        userPhotoStoragePath = null
        selectedUserPhotoUri = null
        saveState()
    }

    fun resetForTesting() {
        clear()
    }
}
