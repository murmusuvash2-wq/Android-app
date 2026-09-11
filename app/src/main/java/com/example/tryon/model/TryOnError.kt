package com.example.tryon.model

/**
 * High-level categories for try-on generation errors.
 */
enum class TryOnErrorCategory {
    INVALID_REQUEST,
    INVALID_PHOTO,
    PRODUCT_UNAVAILABLE,
    GENERATION_FAILED,
    CANCELLED,
    TIMEOUT,
    UNKNOWN
}

/**
 * Typed, domain-level representation of try-on generation errors.
 * Provides safe, TiHin-friendly user-facing error messages without exposing technical stacktraces.
 */
sealed class TryOnError(
    val category: TryOnErrorCategory,
    val userMessage: String,
    val technicalDetails: String? = null
) {
    object InvalidRequest : TryOnError(
        category = TryOnErrorCategory.INVALID_REQUEST,
        userMessage = "Invalid try-on request. Please ensure an outfit and photo are selected."
    )

    object InvalidPhoto : TryOnError(
        category = TryOnErrorCategory.INVALID_PHOTO,
        userMessage = "Could not process this photo. Please choose or capture another photo."
    )

    object ProductUnavailable : TryOnError(
        category = TryOnErrorCategory.PRODUCT_UNAVAILABLE,
        userMessage = "The selected product is currently unavailable for try-on."
    )

    data class GenerationFailed(
        val reason: String? = null
    ) : TryOnError(
        category = TryOnErrorCategory.GENERATION_FAILED,
        userMessage = "We couldn't generate your look right now. Please try again.",
        technicalDetails = reason
    )

    object Cancelled : TryOnError(
        category = TryOnErrorCategory.CANCELLED,
        userMessage = "Try-on generation was cancelled."
    )

    object Timeout : TryOnError(
        category = TryOnErrorCategory.TIMEOUT,
        userMessage = "Generation took longer than expected. Please try again."
    )

    data class Unknown(
        val detail: String? = null
    ) : TryOnError(
        category = TryOnErrorCategory.UNKNOWN,
        userMessage = "Something went wrong while creating your look. Please try again.",
        technicalDetails = detail
    )
}

/**
 * Exception thrown by generation services or components to propagate [TryOnError].
 */
class TryOnException(
    val error: TryOnError,
    cause: Throwable? = null
) : Exception(error.userMessage, cause)
