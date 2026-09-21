package com.example.data.datasource

import com.example.data.model.HeroLook
import com.example.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

class SupabaseProductDataSource(
    private val api: SupabaseProductApi = SupabaseConfig.productApi,
    private val apiKey: String = SupabaseConfig.anonKey
) : ProductDataSource {

    private val cachedProducts = CopyOnWriteArrayList<Product>(MockProductDataSource.CATALOG_PRODUCTS)
    private var hasFetchedRemotely: Boolean = false

    override fun getProducts(): List<Product> = cachedProducts.toList()

    override fun getProductById(id: String): Product? {
        val inCache = cachedProducts.firstOrNull { it.id == id }
        if (inCache != null) return inCache

        // Check MockProductDataSource for legacy aliases and hero looks linkage
        return MockProductDataSource.getProductById(id)
    }

    override fun getTrendingProducts(): List<Product> {
        return if (hasFetchedRemotely && cachedProducts.size > 12) {
            val heroIds = getHeroLooks().map { it.productId }.toSet()
            cachedProducts.filter { it.id !in heroIds }.take(10)
        } else {
            MockProductDataSource.HOME_TRENDING_PRODUCTS
        }
    }

    override fun getHeroLooks(): List<HeroLook> {
        // Curated See the Magic showcase hero looks preserved separately
        return MockProductDataSource.HERO_LOOKS
    }

    override suspend fun fetchProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val allProducts = mutableListOf<Product>()
            var offset = 0
            val pageSize = 1000
            var hasMore = true

            while (hasMore) {
                val authHeader = "Bearer $apiKey"
                val dtos = api.getActiveProducts(
                    apiKey = apiKey,
                    authHeader = authHeader,
                    isActive = "eq.true",
                    order = "created_at.desc",
                    limit = pageSize,
                    offset = offset
                )

                if (dtos.isEmpty()) {
                    hasMore = false
                } else {
                    for (dto in dtos) {
                        allProducts.add(dto.toDomainProduct())
                    }
                    if (dtos.size < pageSize) {
                        hasMore = false
                    } else {
                        offset += pageSize
                    }
                }
            }

            if (allProducts.isNotEmpty()) {
                cachedProducts.clear()
                cachedProducts.addAll(allProducts)
                hasFetchedRemotely = true
            }
            Result.success(allProducts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
