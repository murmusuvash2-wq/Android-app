package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Shared in-memory state repository for the OnMe application.
 *
 * Provides a single source of truth for:
 * 1. Favourited items across Home, Discover, and Looks.
 * 2. Price tracked products (including automatic tracking when a product is favourited).
 * 3. Saved try-on generated results (appearing at the top of Looks -> Recent).
 */
object OnMeStyleRepository {

    // Set of product IDs currently marked as favourite
    var favouriteProductIds by mutableStateOf<Set<String>>(emptySet())
        private set

    // Set of product IDs currently tracked for price drops
    var trackedProductIds by mutableStateOf<Set<String>>(setOf("p1", "p3"))
        private set

    // Saved TryOn results. New results are prepended to appear before fixtures/mocks.
    var savedResults by mutableStateOf<List<TryOnResult>>(emptyList())
        private set

    // Dynamic catalog of tracked products for display in Looks -> Price Tracking
    var customTrackedProducts by mutableStateOf<List<TrackedProduct>>(emptyList())
        private set

    /**
     * Resets repository state to default initial conditions.
     * Useful for testing and state resets.
     */
    fun resetForTesting(
        initialFavourites: Set<String> = emptySet(),
        initialTracked: Set<String> = setOf("p1", "p3"),
        initialSavedResults: List<TryOnResult> = emptyList(),
        initialCustomTracked: List<TrackedProduct> = emptyList()
    ) {
        favouriteProductIds = initialFavourites
        trackedProductIds = initialTracked
        savedResults = initialSavedResults
        customTrackedProducts = initialCustomTracked
    }

    /**
     * Checks whether a given product ID is in favourites.
     */
    fun isFavourite(productId: String): Boolean {
        return favouriteProductIds.contains(productId)
    }

    /**
     * Checks whether a given product ID is currently tracked for price drops.
     */
    fun isPriceTracked(productId: String): Boolean {
        return trackedProductIds.contains(productId)
    }

    /**
     * Toggles favourite state for a product.
     * Rule: When favouriting a product, price tracking is automatically enabled.
     * Rule: When unfavouriting a product, favourite status is removed; price tracking status is not forcibly removed.
     *
     * @param product Optional metadata if available (for populating price tracking card).
     * @return New favourite status (true if now favourited, false if unfavourited).
     */
    fun toggleFavourite(
        productId: String,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null
    ): Boolean {
        if (favouriteProductIds.contains(productId)) {
            favouriteProductIds = favouriteProductIds - productId
            return false
        } else {
            favouriteProductIds = favouriteProductIds + productId
            // Rule: Favouriting automatically enables price tracking
            setPriceTracking(
                productId = productId,
                enabled = true,
                productName = productName,
                merchant = merchant,
                price = price,
                imageUrl = imageUrl
            )
            return true
        }
    }

    /**
     * Explicitly sets favourite state.
     */
    fun setFavourite(
        productId: String,
        isFav: Boolean,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null
    ) {
        if (isFav) {
            favouriteProductIds = favouriteProductIds + productId
            setPriceTracking(
                productId = productId,
                enabled = true,
                productName = productName,
                merchant = merchant,
                price = price,
                imageUrl = imageUrl
            )
        } else {
            favouriteProductIds = favouriteProductIds - productId
        }
    }

    /**
     * Toggles price tracking for a product.
     * Rule: Manually turning Price Tracking OFF does NOT remove the favourite.
     */
    fun togglePriceTracking(
        productId: String,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null
    ): Boolean {
        val currentlyTracked = isPriceTracked(productId)
        val newTracked = !currentlyTracked
        setPriceTracking(
            productId = productId,
            enabled = newTracked,
            productName = productName,
            merchant = merchant,
            price = price,
            imageUrl = imageUrl
        )
        return newTracked
    }

    /**
     * Explicitly sets price tracking state.
     * Maintains custom tracked products list for newly tracked items.
     */
    fun setPriceTracking(
        productId: String,
        enabled: Boolean,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null
    ) {
        if (enabled) {
            trackedProductIds = trackedProductIds + productId
            // If we have product metadata and it's not already in customTrackedProducts, add it
            if (productName != null && imageUrl != null) {
                if (customTrackedProducts.none { it.id == productId }) {
                    val newTracked = TrackedProduct(
                        id = productId,
                        productImage = imageUrl,
                        productName = productName,
                        merchant = merchant ?: "",
                        currentPrice = price ?: 0.0,
                        productUrl = "",
                        trackedAt = "Just now"
                    )
                    customTrackedProducts = listOf(newTracked) + customTrackedProducts
                }
            }
        } else {
            trackedProductIds = trackedProductIds - productId
            customTrackedProducts = customTrackedProducts.filterNot { it.id == productId }
        }
    }

    /**
     * Saves a generated TryOn result.
     * Idempotent: If an identical result (by ID or matching resultImage + productId) is already saved,
     * it updates or avoids duplication. Newly saved results are prepended so they appear at the top.
     */
    fun saveResult(result: TryOnResult) {
        val filtered = savedResults.filterNot { it.id == result.id || (it.productId == result.productId && it.resultImage == result.resultImage) }
        savedResults = listOf(result) + filtered
    }

    /**
     * Checks if a result has already been saved.
     */
    fun isResultSaved(productId: String, resultImage: String): Boolean {
        return savedResults.any { it.productId == productId && it.resultImage == resultImage }
    }

    /**
     * Gets all active tracked products, merging custom tracked products with baseline fixtures.
     * Items are filtered by `trackedProductIds`.
     */
    fun getActiveTrackedProducts(baselineFixtures: List<TrackedProduct>): List<TrackedProduct> {
        val allProducts = customTrackedProducts + baselineFixtures.filterNot { fixture ->
            customTrackedProducts.any { it.id == fixture.id }
        }
        return allProducts.filter { trackedProductIds.contains(it.id) }
    }
}
