package com.example.data.repository

import com.example.data.model.Product
import com.example.ui.DiscoverTab

/**
 * Repository interface governing access to catalog and product details in TiHin.
 *
 * Boundaries:
 * - ProductRepository owns catalog products, metadata, search/filtering, and detail lookup.
 * - OnMeStyleRepository owns user-specific state (favourites, price tracking, saved try-on results).
 */
interface ProductRepository {
    /**
     * Returns the complete list of catalog products.
     */
    fun getProducts(): List<Product>

    /**
     * Resolves a single product by ID or legacy alias, returning null if not found.
     */
    fun getProductById(id: String): Product?

    /**
     * Searches catalog products matching title, brand, or description.
     */
    fun searchProducts(query: String): List<Product>

    /**
     * Returns curated trending products for Home and featured showcase displays.
     */
    fun getTrendingProducts(): List<Product>

    /**
     * Returns catalog products ordered according to the selected Discover tab.
     */
    fun getProductsForTab(tab: DiscoverTab): List<Product>

    companion object {
        fun get(): ProductRepository = ProductRepositoryProvider.get()
    }
}
