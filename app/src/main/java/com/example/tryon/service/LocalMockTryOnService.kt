package com.example.tryon.service

import com.example.data.repository.ProductRepositoryProvider
import com.example.tryon.model.TryOnError
import com.example.tryon.model.TryOnException
import com.example.tryon.model.TryOnRequest
import com.example.tryon.model.TryOnResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap

/**
 * Deterministic local implementation of [TryOnService] that powers the app without requiring a live backend.
 *
 * Reuses the existing local product imagery mechanism while cleanly abstracting the generation lifecycle.
 * Fully cancellation-aware and configurable for testing.
 */
class LocalMockTryOnService(
    private val delayMillis: Long = 4500L,
    private val shouldFail: Boolean = false,
    private val failureError: TryOnError = TryOnError.GenerationFailed(),
    private val mockResultImageOverride: String? = null
) : TryOnService {

    private val cancelledRequestIds = ConcurrentHashMap.newKeySet<String>()

    override suspend fun generate(request: TryOnRequest): TryOnResult {
        // Validate request parameters
        if (request.productId.isBlank()) {
            throw TryOnException(TryOnError.InvalidRequest)
        }
        if (request.userPhotoUri.isBlank()) {
            throw TryOnException(TryOnError.InvalidPhoto)
        }

        if (cancelledRequestIds.contains(request.requestId)) {
            throw CancellationException("Request ${request.requestId} was cancelled before generation.")
        }

        // Simulate asynchronous generation latency with cancellation cooperation
        if (delayMillis > 0) {
            try {
                delay(delayMillis)
            } catch (e: CancellationException) {
                cancelledRequestIds.add(request.requestId)
                throw e
            }
        }

        // Post-delay cancellation check
        if (cancelledRequestIds.contains(request.requestId)) {
            throw CancellationException("Request ${request.requestId} was cancelled during generation.")
        }

        if (shouldFail) {
            throw TryOnException(failureError)
        }

        // Resolve deterministic mock visual output based on canonical product catalog
        val product = ProductRepositoryProvider.get().getProductById(request.productId)
        val resultImage = mockResultImageOverride
            ?: product?.primaryImageUrl?.takeIf { it.isNotBlank() }
            ?: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80"

        return TryOnResult(
            requestId = request.requestId,
            generatedImageUri = resultImage,
            productId = request.productId,
            generationTimestamp = System.currentTimeMillis(),
            watermarkApplied = true,
            metadata = mapOf(
                "generation_mode" to "local_mock",
                "resolved_product_brand" to (product?.brand ?: "Unknown")
            )
        )
    }

    override fun cancel(requestId: String) {
        cancelledRequestIds.add(requestId)
    }
}
