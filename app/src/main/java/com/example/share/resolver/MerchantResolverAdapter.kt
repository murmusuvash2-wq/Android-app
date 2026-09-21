package com.example.share.resolver

import com.example.share.model.SharedProductInput

/**
 * Adapter contract for merchant-specific product parsing and identification.
 * Allows independent integration of merchants (e.g. Zara, H&M, Myntra, Amazon, Flipkart, Ajio).
 */
interface MerchantResolverAdapter {
    /**
     * Display name or brand identity of this merchant.
     */
    val merchantName: String

    /**
     * Determines whether this adapter can process the incoming shared product input.
     */
    fun canHandle(input: SharedProductInput): Boolean

    /**
     * Resolves the canonical product ID from the input without querying ProductRepository directly.
     * Returns null if the item cannot be resolved within this merchant.
     */
    fun resolveCanonicalProductId(input: SharedProductInput): String?
}
