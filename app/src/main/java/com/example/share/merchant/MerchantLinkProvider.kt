package com.example.share.merchant

/**
 * Abstraction for merchant purchase and deep links.
 *
 * Preserves the original merchant URL associated with a shared product while
 * establishing an extension point for future merchant-specific affiliate tracking
 * or deep-link injection without generic URL rewriting.
 */
interface MerchantLinkProvider {
    /**
     * Resolves the purchase URL for a product, preserving the original shared
     * merchant URL when provided.
     */
    fun getPurchaseUrl(originalUrl: String, merchant: String? = null): String

    companion object {
        fun get(): MerchantLinkProvider = DefaultMerchantLinkProvider
    }
}

/**
 * Default implementation preserving original merchant URLs strictly untouched.
 */
object DefaultMerchantLinkProvider : MerchantLinkProvider {
    override fun getPurchaseUrl(originalUrl: String, merchant: String?): String {
        return originalUrl
    }
}
