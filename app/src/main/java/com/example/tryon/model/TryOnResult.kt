package com.example.tryon.model

/**
 * Clean, immutable domain model representing the output of a virtual try-on generation.
 *
 * Support:
 * - [requestId]: matches the originating [TryOnRequest.requestId] for idempotency tracking
 * - [generatedImageUri]: URI or local reference to the generated try-on imagery
 * - [productId]: canonical product identifier
 * - [generationTimestamp]: timestamp when generation was completed
 * - [watermarkApplied]: whether a watermark is currently present on the generated image
 * - [metadata]: optional generation and processing metadata
 */
data class TryOnResult(
    val requestId: String,
    val generatedImageUri: String,
    val productId: String,
    val generationTimestamp: Long = System.currentTimeMillis(),
    val watermarkApplied: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(requestId.isNotBlank()) { "requestId must not be blank" }
        require(productId.isNotBlank()) { "productId must not be blank" }
        require(generatedImageUri.isNotBlank()) { "generatedImageUri must not be blank" }
    }
}
