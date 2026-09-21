package com.example.share.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Product
import com.example.share.receiver.ShareIntentHandler
import com.example.share.receiver.ShareIntentResult
import com.example.share.resolver.ProductResolutionResult
import com.example.share.resolver.ProductResolver
import com.example.ui.TryOnManager

sealed interface ShareResolutionOutcome {
    data class GoToTryOn(val product: Product) : ShareResolutionOutcome
    data class GoToImageTryOn(val imageUri: Uri) : ShareResolutionOutcome
    data class ShowError(val message: String) : ShareResolutionOutcome
    data object None : ShareResolutionOutcome
}

object ShareFlowManager {
    fun processIntent(intent: Intent?, context: Context? = null): ShareResolutionOutcome {
        val result = ShareIntentHandler.extract(intent)
        return when (result) {
            is ShareIntentResult.ValidTextShare -> {
                when (val resolved = ProductResolver.get().resolve(result.input)) {
                    is ProductResolutionResult.Success -> {
                        val product = resolved.product
                        TryOnManager.selectedProductId = product.id
                        TryOnManager.selectedProductName = product.name
                        TryOnManager.selectedProductBrand = product.brand
                        TryOnManager.selectedProductPrice = product.price
                        TryOnManager.selectedProductImage = product.primaryImageUrl
                        ShareResolutionOutcome.GoToTryOn(product)
                    }
                    is ProductResolutionResult.Failure -> {
                        ShareResolutionOutcome.ShowError(resolved.message)
                    }
                }
            }
            is ShareIntentResult.ValidImageShare -> {
                TryOnManager.selectedUserPhotoUri = result.imageUri
                ShareResolutionOutcome.GoToImageTryOn(result.imageUri)
            }
            is ShareIntentResult.Malformed -> {
                ShareResolutionOutcome.ShowError("Could not recognize a valid product link in the shared text.")
            }
            is ShareIntentResult.Empty,
            is ShareIntentResult.NotShareIntent -> {
                ShareResolutionOutcome.None
            }
        }
    }
}
