package com.example.share.resolver

import com.example.data.repository.ProductRepository
import com.example.share.model.SharedProductInput

/**
 * Default implementation of ProductResolver.
 * Coordinates merchant adapters and resolves canonical products via ProductRepository.
 */
class DefaultProductResolver(
    private val adapters: List<MerchantResolverAdapter> = defaultAdapters(),
    private val productRepository: ProductRepository = ProductRepository.get()
) : ProductResolver {

    override fun resolve(input: SharedProductInput): ProductResolutionResult {
        if (input.sourceUrl.isBlank()) {
            return ProductResolutionResult.Failure.EmptyInput()
        }

        // 1. Find matching merchant adapter
        val adapter = adapters.firstOrNull { it.canHandle(input) }
            ?: return ProductResolutionResult.Failure.UnsupportedMerchant(input.sourceUrl)

        // 2. Resolve canonical product ID
        val canonicalId = adapter.resolveCanonicalProductId(input)
            ?: return ProductResolutionResult.Failure.UnsupportedMerchant(input.sourceUrl)

        // 3. Resolve canonical Product from ProductRepository
        val product = productRepository.getProductById(canonicalId)
            ?: return ProductResolutionResult.Failure.ProductNotFound(canonicalId)

        return ProductResolutionResult.Success(
            product = product,
            originalUrl = input.sourceUrl,
            merchantName = adapter.merchantName
        )
    }

    companion object {
        fun defaultAdapters(): List<MerchantResolverAdapter> = listOf(
            ZaraResolverAdapter(),
            HnMResolverAdapter(),
            MangoResolverAdapter(),
            UrbanicResolverAdapter(),
            AsosResolverAdapter(),
            MyntraResolverAdapter(),
            AmazonResolverAdapter(),
            FlipkartResolverAdapter(),
            AjioResolverAdapter(),
            GenericSupportedUrlAdapter()
        )
    }
}
