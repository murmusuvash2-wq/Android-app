package com.example.share.resolver

import com.example.share.model.SharedProductInput

/**
 * High-level resolver contract mapping shared inputs to canonical Product domain models.
 */
interface ProductResolver {
    /**
     * Resolves an incoming shared product input to a canonical Product from ProductRepository.
     */
    fun resolve(input: SharedProductInput): ProductResolutionResult

    companion object {
        fun get(): ProductResolver = ProductResolverProvider.get()
    }
}
