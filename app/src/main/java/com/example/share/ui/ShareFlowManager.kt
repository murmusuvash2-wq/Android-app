package com.example.share.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.model.Product
import com.example.share.data.SharedProductRepository
import com.example.share.data.SharedProductRepositoryProvider
import com.example.share.model.SharedProductInput
import com.example.share.receiver.ShareIntentHandler
import com.example.share.receiver.ShareIntentResult
import com.example.share.resolver.ProductResolutionResult
import com.example.share.resolver.ProductResolver
import com.example.share.resolver.ProductResolverProvider
import com.example.ui.TryOnManager

/**
 * Outcome resulting from processing an incoming share intent.
 */
sealed interface ShareResolutionOutcome {
    data class GoToTryOn(val product: Product) : ShareResolutionOutcome
    data class GoToImageTryOn(val imageUri: Uri) : ShareResolutionOutcome
    data class ShowError(val message: String) : ShareResolutionOutcome
    object None : ShareResolutionOutcome
}

/**
 * Coordinates share processing, product resolution, Try-On preselection, and graceful error states.
 */
object ShareFlowManager {
    var lastSharedInput by mutableStateOf<SharedProductInput?>(null)
    var lastErrorMessage by mutableStateOf("We couldn't recognize this product")
    var lastResolvedProduct by mutableStateOf<Product?>(null)

    /**
     * Processes an incoming Android intent.
     * Consumes 0 credits.
     */
    fun processIntent(
        intent: Intent?,
        context: Context? = null,
        resolver: ProductResolver = ProductResolverProvider.get(),
        sharedProductRepository: SharedProductRepository = SharedProductRepositoryProvider.get()
    ): ShareResolutionOutcome {
        val result = ShareIntentHandler.extract(intent)

        return when (result) {
            is ShareIntentResult.NotShareIntent -> {
                ShareResolutionOutcome.None
            }
            is ShareIntentResult.ValidImageShare -> {
                TryOnManager.selectedUserPhotoUri = result.imageUri.toString()
                ShareResolutionOutcome.GoToImageTryOn(result.imageUri)
            }
            is ShareIntentResult.ValidTextShare -> {
                lastSharedInput = result.input
                when (val resolution = resolver.resolve(result.input)) {
                    is ProductResolutionResult.Success -> {
                        lastResolvedProduct = resolution.product
                        // Preselect in Try-On without asking user again
                        TryOnManager.selectProduct(resolution.product, resolution.originalUrl)
                        // Save to shared product history
                        sharedProductRepository.saveSharedProduct(
                            product = resolution.product,
                            originalUrl = resolution.originalUrl,
                            merchantName = resolution.merchantName
                        )
                        ShareResolutionOutcome.GoToTryOn(resolution.product)
                    }
                    is ProductResolutionResult.Failure -> {
                        lastErrorMessage = resolution.message
                        ShareResolutionOutcome.ShowError(resolution.message)
                    }
                }
            }
            is ShareIntentResult.Malformed -> {
                lastSharedInput = null
                lastErrorMessage = "We couldn't recognize this product"
                ShareResolutionOutcome.ShowError(lastErrorMessage)
            }
            is ShareIntentResult.Empty -> {
                lastSharedInput = null
                lastErrorMessage = "We couldn't recognize this product"
                ShareResolutionOutcome.ShowError(lastErrorMessage)
            }
        }
    }

    /**
     * Retries resolution of the last received shared product input.
     */
    fun retryResolution(
        resolver: ProductResolver = ProductResolverProvider.get(),
        sharedProductRepository: SharedProductRepository = SharedProductRepositoryProvider.get()
    ): ShareResolutionOutcome {
        val input = lastSharedInput ?: return ShareResolutionOutcome.ShowError("We couldn't recognize this product")
        return when (val resolution = resolver.resolve(input)) {
            is ProductResolutionResult.Success -> {
                lastResolvedProduct = resolution.product
                TryOnManager.selectProduct(resolution.product, resolution.originalUrl)
                sharedProductRepository.saveSharedProduct(
                    product = resolution.product,
                    originalUrl = resolution.originalUrl,
                    merchantName = resolution.merchantName
                )
                ShareResolutionOutcome.GoToTryOn(resolution.product)
            }
            is ProductResolutionResult.Failure -> {
                lastErrorMessage = resolution.message
                ShareResolutionOutcome.ShowError(resolution.message)
            }
        }
    }

    fun resetForTesting() {
        lastSharedInput = null
        lastErrorMessage = "We couldn't recognize this product"
        lastResolvedProduct = null
    }
}
