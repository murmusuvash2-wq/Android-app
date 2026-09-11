package com.example.tryon.model

/**
 * Type-safe state representing the lifecycle of a try-on generation.
 * Avoids arbitrary string comparisons across the presentation and orchestration layers.
 */
sealed interface GenerationStatus {

    /**
     * No active generation. Ready to accept a new request.
     */
    object Idle : GenerationStatus

    /**
     * Generation is actively in progress.
     */
    data class Processing(
        val requestId: String,
        val progress: Float = 0f
    ) : GenerationStatus

    /**
     * Generation successfully completed with a valid [TryOnResult].
     */
    data class Success(
        val result: TryOnResult
    ) : GenerationStatus

    /**
     * Generation failed with a structured [TryOnError].
     */
    data class Failed(
        val error: TryOnError
    ) : GenerationStatus

    /**
     * Generation was explicitly cancelled by the user or navigation.
     */
    data class Cancelled(
        val requestId: String? = null
    ) : GenerationStatus
}
