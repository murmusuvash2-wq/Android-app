package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.local.AppDatabase
import com.example.data.local.TryOnPhotoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Domain model representing a user Try-On reference photo.
 */
data class TryOnPhoto(
    val id: String,
    val uri: String,
    val isDefault: Boolean = false,
    val orderIndex: Int = 0
)

/**
 * Single source of truth for user Try-On reference photos.
 * Enforces the maximum 5 photos constraint, default photo selection,
 * safe internal storage persistence, and process restart survival.
 */
object UserPhotosRepository {
    const val MAX_PHOTOS = 5

    val DEFAULT_PHOTOS = listOf(
        TryOnPhoto(
            id = "photo_1",
            uri = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80",
            isDefault = true,
            orderIndex = 0
        ),
        TryOnPhoto(
            id = "photo_2",
            uri = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=800&q=80",
            isDefault = false,
            orderIndex = 1
        ),
        TryOnPhoto(
            id = "photo_3",
            uri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&q=80",
            isDefault = false,
            orderIndex = 2
        )
    )

    var photos by mutableStateOf<List<TryOnPhoto>>(DEFAULT_PHOTOS)
        private set

    val defaultPhoto: TryOnPhoto?
        get() = photos.firstOrNull { it.isDefault } ?: photos.firstOrNull()

    val defaultPhotoUri: String?
        get() = defaultPhoto?.uri

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Initializes user photos from the Room database off the main thread.
     * Seeds default 3 photos if storage is empty for this user.
     */
    fun init(context: Context, userId: String = UserProfileRepository.profile.userId) {
        scope.launch {
            initInternal(context, userId)
        }
    }

    suspend fun initSuspend(context: Context, userId: String = UserProfileRepository.profile.userId) {
        withContext(Dispatchers.IO) {
            initInternal(context, userId)
        }
    }

    fun initSync(context: Context, userId: String = UserProfileRepository.profile.userId) {
        runBlocking(Dispatchers.IO) {
            initInternal(context, userId)
        }
    }

    private fun initInternal(context: Context, userId: String) {
        try {
            val db = AppDatabase.getInstance(context)
            val stored = db.tryOnPhotoDao().getPhotosSync(userId)
            if (stored.isNotEmpty()) {
                photos = stored.map {
                    TryOnPhoto(
                        id = it.id,
                        uri = it.uri,
                        isDefault = it.isDefault,
                        orderIndex = it.orderIndex
                    )
                }
            } else {
                // Seed initial default photos for authenticated user
                photos = DEFAULT_PHOTOS
                val entities = DEFAULT_PHOTOS.mapIndexed { index, photo ->
                    TryOnPhotoEntity(
                        id = photo.id,
                        uri = photo.uri,
                        isDefault = photo.isDefault,
                        orderIndex = index,
                        userId = userId
                    )
                }
                db.tryOnPhotoDao().insertAllSync(entities)
            }
        } catch (e: Exception) {
            photos = DEFAULT_PHOTOS
        }
    }

    /**
     * Adds a new Try-On photo.
     * Enforces the max 5 photos limit.
     * Returns true if added, false if maximum reached or error.
     */
    fun addPhoto(
        context: Context?,
        uriString: String,
        setAsDefault: Boolean = false,
        userId: String = UserProfileRepository.profile.userId
    ): Boolean {
        return addPhotoInternal(context, uriString, setAsDefault, userId, synchronous = false)
    }

    fun addPhotoSync(
        context: Context?,
        uriString: String,
        setAsDefault: Boolean = false,
        userId: String = UserProfileRepository.profile.userId
    ): Boolean {
        return addPhotoInternal(context, uriString, setAsDefault, userId, synchronous = true)
    }

    private fun addPhotoInternal(
        context: Context?,
        uriString: String,
        setAsDefault: Boolean = false,
        userId: String = UserProfileRepository.profile.userId,
        synchronous: Boolean = false
    ): Boolean {
        if (photos.size >= MAX_PHOTOS) {
            return false
        }

        val persistentUri = if (context != null) {
            persistImageToInternal(context, uriString)
        } else {
            uriString
        }

        val newId = "photo_${UUID.randomUUID().toString().take(8)}"
        val isFirst = photos.isEmpty()
        val shouldBeDefault = setAsDefault || isFirst

        val updatedExisting = if (shouldBeDefault) {
            photos.map { it.copy(isDefault = false) }
        } else {
            photos
        }

        val newPhoto = TryOnPhoto(
            id = newId,
            uri = persistentUri,
            isDefault = shouldBeDefault,
            orderIndex = photos.size
        )

        photos = updatedExisting + newPhoto

        context?.let { ctx ->
            if (synchronous) {
                runBlocking(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getInstance(ctx)
                        if (shouldBeDefault) {
                            db.tryOnPhotoDao().clearDefaultSync(userId)
                        }
                        db.tryOnPhotoDao().insertSync(
                            TryOnPhotoEntity(
                                id = newPhoto.id,
                                uri = newPhoto.uri,
                                isDefault = newPhoto.isDefault,
                                orderIndex = newPhoto.orderIndex,
                                userId = userId
                            )
                        )
                    } catch (e: Exception) {
                        // Safe fallback
                    }
                }
            } else {
                scope.launch {
                    try {
                        val db = AppDatabase.getInstance(ctx)
                        if (shouldBeDefault) {
                            db.tryOnPhotoDao().clearDefault(userId)
                        }
                        db.tryOnPhotoDao().insert(
                            TryOnPhotoEntity(
                                id = newPhoto.id,
                                uri = newPhoto.uri,
                                isDefault = newPhoto.isDefault,
                                orderIndex = newPhoto.orderIndex,
                                userId = userId
                            )
                        )
                    } catch (e: Exception) {
                        // Safe fallback
                    }
                }
            }
        }
        return true
    }

    /**
     * Sets a specified photo as the default Try-On photo.
     */
    fun setDefaultPhoto(
        context: Context?,
        photoId: String,
        userId: String = UserProfileRepository.profile.userId
    ) {
        photos = photos.map { photo ->
            photo.copy(isDefault = (photo.id == photoId))
        }

        context?.let { ctx ->
            scope.launch {
                try {
                    val db = AppDatabase.getInstance(ctx)
                    db.tryOnPhotoDao().clearDefault(userId)
                    db.tryOnPhotoDao().setDefault(photoId, userId)
                } catch (e: Exception) {
                    // Safe fallback
                }
            }
        }
    }

    /**
     * Deletes a Try-On photo.
     * If the deleted photo was the default photo, designates the next available photo as default.
     */
    fun deletePhoto(
        context: Context?,
        photoId: String,
        userId: String = UserProfileRepository.profile.userId
    ) {
        val target = photos.firstOrNull { it.id == photoId } ?: return
        val remaining = photos.filterNot { it.id == photoId }

        photos = if (target.isDefault && remaining.isNotEmpty()) {
            remaining.mapIndexed { index, photo ->
                photo.copy(isDefault = (index == 0))
            }
        } else {
            remaining
        }

        context?.let { ctx ->
            scope.launch {
                try {
                    val db = AppDatabase.getInstance(ctx)
                    db.tryOnPhotoDao().deleteById(photoId)
                    if (target.isDefault && remaining.isNotEmpty()) {
                        db.tryOnPhotoDao().setDefault(remaining.first().id, userId)
                    }
                    // Clean up internal file if local
                    if (target.uri.startsWith("file://") || target.uri.startsWith("/")) {
                        val file = File(Uri.parse(target.uri).path ?: target.uri)
                        if (file.exists() && file.parentFile?.name == "tryon_photos") {
                            file.delete()
                        }
                    }
                } catch (e: Exception) {
                    // Safe fallback
                }
            }
        }
    }

    /**
     * Copies external content URI to private app storage to guarantee permanent persistence.
     */
    fun persistImageToInternal(context: Context, sourceUriString: String): String {
        return try {
            if (sourceUriString.startsWith("http://") || sourceUriString.startsWith("https://")) {
                return sourceUriString
            }
            val uri = Uri.parse(sourceUriString)
            val dir = File(context.filesDir, "tryon_photos").apply { mkdirs() }
            val destFile = File(dir, "photo_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
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
        initialPhotos: List<TryOnPhoto> = DEFAULT_PHOTOS,
        context: Context? = null
    ) {
        photos = initialPhotos
        context?.let { ctx ->
            runCatching {
                runBlocking(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(ctx)
                    db.tryOnPhotoDao().deleteAllSync(UserProfileRepository.profile.userId)
                }
            }
        }
    }
}
