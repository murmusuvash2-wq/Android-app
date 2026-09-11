package com.example.tryon.model

import java.util.UUID

/**
 * Metadata about the user photo used for try-on generation.
 */
data class UserPhotoMetadata(
    val uri: String,
    val source: String? = null, // e.g., "gallery", "camera", "profile"
    val width: Int? = null,
    val height: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Clean, immutable domain model representing an input request for virtual try-on generation.
 *
 * Contains only data required by generation services:
 * - [requestId]: unique, stable identifier for generation idempotency
 * - [productId]: canonical product identifier owned by ProductRepository
 * - [userPhotoUri]: URI or local reference to user photo
 * - [userPhotoMetadata]: optional photo metadata if available
 * - [createdAt]: request creation timestamp
 * - [metadata]: extensible future-safe request metadata
 *
 * Does NOT store the full Product object or UI state.
 */
data class TryOnRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val productId: String,
    val userPhotoUri: String,
    val userPhotoMetadata: UserPhotoMetadata? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(requestId.isNotBlank()) { "requestId must not be blank" }
        require(productId.isNotBlank()) { "productId must not be blank" }
        require(userPhotoUri.isNotBlank()) { "userPhotoUri must not be blank" }
    }
}
