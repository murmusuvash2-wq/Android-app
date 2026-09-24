package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import java.io.File

data class TryOnPhoto(
    val uri: Uri,
    val label: String
)

object TryOnManager {
    private const val PREFS_NAME = "tihin_tryon_prefs"
    private const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"
    private const val KEY_TRYON_PHOTOS = "tryon_photos"
    private const val MAX_TRYON_PHOTOS = 5

    private val defaultLabels = listOf("Front", "Side", "Casual", "Different Outfit", "Backup")

    var selectedProductId: String? by mutableStateOf(null)
    var selectedProductName: String? by mutableStateOf(null)
    var selectedProductBrand: String? by mutableStateOf(null)
    var selectedProductPrice: Double by mutableStateOf(0.0)
    var selectedProductImage: String? by mutableStateOf(null)

    var profilePhotoUri: Uri? by mutableStateOf(null)
    var tryOnPhotos: List<TryOnPhoto> by mutableStateOf(emptyList())
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
        profilePhotoUri = prefs.getString(KEY_PROFILE_PHOTO_URI, null)?.let(Uri::parse)

        val stored = prefs.getString(KEY_TRYON_PHOTOS, null).orEmpty()
        tryOnPhotos = stored.split("|").mapNotNull { item ->
            val parts = item.split("::", limit = 2)
            if (parts.size == 2 && parts[0].isNotBlank()) {
                TryOnPhoto(Uri.parse(parts[0]), parts[1])
            } else null
        }.take(MAX_TRYON_PHOTOS)

        // Migrate the old single-photo setting into the new Try-On photo collection.
        if (tryOnPhotos.isEmpty()) {
            prefs.getString("user_photo_uri", null)?.let { old ->
                if (old.isNotBlank()) {
                    tryOnPhotos = listOf(TryOnPhoto(Uri.parse(old), defaultLabels[0]))
                    saveState()
                }
            }
        }

        if (selectedUserPhotoUri == null) {
            selectedUserPhotoUri = tryOnPhotos.firstOrNull()?.uri
        }
    }

    private fun saveState() {
        val context = appContext ?: return
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PROFILE_PHOTO_URI, profilePhotoUri?.toString())
            .putString(
                KEY_TRYON_PHOTOS,
                tryOnPhotos.joinToString("|") { "${it.uri}::${it.label}" }
            )
            .apply()
    }

    fun setProfilePhoto(uriString: String) {
        profilePhotoUri = Uri.parse(uriString)
        saveState()
    }

    fun clearProfilePhoto() {
        profilePhotoUri = null
        saveState()
    }

    fun addTryOnPhoto(uriString: String, label: String? = null): Boolean {
        if (tryOnPhotos.size >= MAX_TRYON_PHOTOS) return false
        val nextLabel = label ?: defaultLabels[tryOnPhotos.size]
        val photo = TryOnPhoto(Uri.parse(uriString), nextLabel)
        tryOnPhotos = tryOnPhotos + photo
        selectedUserPhotoUri = photo.uri
        saveState()
        return true
    }

    fun replaceTryOnPhoto(index: Int, uriString: String): Boolean {
        if (index !in tryOnPhotos.indices) return false
        val updated = tryOnPhotos.toMutableList()
        updated[index] = TryOnPhoto(Uri.parse(uriString), updated[index].label)
        tryOnPhotos = updated
        selectedUserPhotoUri = updated[index].uri
        saveState()
        return true
    }

    fun removeTryOnPhoto(index: Int) {
        if (index !in tryOnPhotos.indices) return
        tryOnPhotos = tryOnPhotos.toMutableList().also { it.removeAt(index) }
        tryOnPhotos = tryOnPhotos.mapIndexed { i, photo ->
            photo.copy(label = defaultLabels.getOrElse(i) { photo.label })
        }
        if (selectedUserPhotoUri !in tryOnPhotos.map { it.uri }) {
            selectedUserPhotoUri = tryOnPhotos.firstOrNull()?.uri
        }
        saveState()
    }

    fun selectTryOnPhoto(uri: Uri?) {
        selectedUserPhotoUri = uri
    }

    fun updateUserPhoto(uriString: String, context: Context? = null) {
        appContext = (context ?: appContext)?.applicationContext
        if (tryOnPhotos.isEmpty()) addTryOnPhoto(uriString)
        else {
            replaceTryOnPhoto(0, uriString)
        }
    }

    fun getTryOnPhotoCount(): Int = tryOnPhotos.size
    fun canAddTryOnPhoto(): Boolean = tryOnPhotos.size < MAX_TRYON_PHOTOS

    fun createTempCameraUri(context: Context): Uri {
        return try {
            val imagePath = File(context.cacheDir, "camera_photos")
            if (!imagePath.exists()) imagePath.mkdirs()
            val newFile = File(imagePath, "temp_camera_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
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

    fun resetTestingPhotos() {
        profilePhotoUri = null
        tryOnPhotos = emptyList()
        selectedUserPhotoUri = null
        saveState()
    }
}
