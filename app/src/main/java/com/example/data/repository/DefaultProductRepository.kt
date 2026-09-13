package com.example.data.repository

import com.example.data.datasource.MockProductDataSource
import com.example.data.datasource.ProductDataSource
import com.example.data.model.Product
import com.example.data.model.HeroLook
import com.example.ui.DiscoverTab
import com.example.ui.getProductsForTab

/**
 * Default implementation of ProductRepository backed by an injectable ProductDataSource.
 */
class DefaultProductRepository(
    private val dataSource: ProductDataSource = MockProductDataSource
) : ProductRepository {

    override fun getProducts(): List<Product> {
        return dataSource.getProducts()
    }

    override fun getProductById(id: String): Product? {
        return dataSource.getProductById(id)
    }

    override fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return getProducts()
        val trimmed = query.trim()
        return getProducts().filter { product ->
            product.name.contains(trimmed, ignoreCase = true) ||
            product.brand.contains(trimmed, ignoreCase = true) ||
            product.description?.contains(trimmed, ignoreCase = true) == true
        }
    }

    override fun getTrendingProducts(): List<Product> {
        return dataSource.getTrendingProducts()
    }

    override fun getProductsForTab(tab: DiscoverTab): List<Product> {
        return getProductsForTab(getProducts(), tab)
    }
    
    override fun getHeroLooks(): List<HeroLook> {
        return dataSource.getHeroLooks()
    }
}
