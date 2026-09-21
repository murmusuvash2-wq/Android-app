package com.example.share.resolver

import com.example.data.model.Product

/**
 * Result representing the outcome of resolving a shared product input.
 */
sealed interface ProductResolutionResult {
    /**
     * Successfully resolved to an existing canonical Product from ProductRepository.
     */
    data class Success(
        val product: Product,
        val originalUrl: String,
        val merchantName: String?
    ) : ProductResolutionResult

    /**
     * Resolution failed. Captures reason and standard user-facing message.
     */
    sealed interface Failure : ProductResolutionResult {
        val message: String

        data class UnsupportedMerchant(
            val url: String,
            override val message: String = "We couldn't recognize this product"
        ) : Failure

        data class ProductNotFound(
            val identifier: String,
            override val message: String = "We couldn't recognize this product"
        ) : Failure

        data class MalformedInput(
            val raw: String?,
            override val message: String = "We couldn't recognize this product"
        ) : Failure

        data class EmptyInput(
            override val message: String = "We couldn't recognize this product"
        ) : Failure
    }
}
