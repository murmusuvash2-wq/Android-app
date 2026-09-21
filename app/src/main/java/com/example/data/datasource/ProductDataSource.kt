package com.example.data.datasource

import com.example.data.model.Product
import com.example.data.model.HeroLook

/**
 * Data source abstraction for accessing catalog product data.
 * Backed by mock fixtures today; future implementations will connect to remote catalog APIs.
 */
interface ProductDataSource {
    /**
     * Returns the complete catalog of products.
     */
    fun getProducts(): List<Product>

    /**
     * Resolves a single product by ID or alias, returning null if not found.
     */
    fun getProductById(id: String): Product?

    /**
     * Returns curated trending products for Home and featured showcase displays.
     */
    fun getTrendingProducts(): List<Product>
    fun getHeroLooks(): List<HeroLook>

    /**
     * Asynchronously fetches and updates the remote product catalog.
     */
    suspend fun fetchProducts(): Result<List<Product>> = Result.success(getProducts())
}
