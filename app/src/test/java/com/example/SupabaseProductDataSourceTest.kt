package com.example

import com.example.data.datasource.SupabaseProductApi
import com.example.data.datasource.SupabaseProductDataSource
import com.example.data.datasource.SupabaseProductDto
import com.example.data.repository.DefaultProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SupabaseProductDataSourceTest {

    @Test
    fun `test SupabaseProductDto mapping to canonical Product`() {
        val dto = SupabaseProductDto(
            id = "test-uuid-1",
            name = "Linen Summer Dress",
            brand = "ZARA",
            productImages = listOf("https://example.com/img1.jpg", "https://example.com/img2.jpg"),
            price = 4999.0,
            originalPrice = 6999.0,
            rating = 4.7,
            reviewCount = 50,
            description = "Lightweight breathable linen dress",
            styleTip = "Pair with espadrilles",
            loveCount = 120,
            tryOnCount = 45,
            salesCount = 80,
            trendingScore = 95.5
        )

        val product = dto.toDomainProduct()

        assertEquals("test-uuid-1", product.id)
        assertEquals("Linen Summer Dress", product.name)
        assertEquals("ZARA", product.brand)
        assertEquals(2, product.productImages.size)
        assertEquals("https://example.com/img1.jpg", product.primaryImageUrl)
        assertEquals(4999.0, product.price, 0.001)
        assertEquals(6999.0, product.originalPrice!!, 0.001)
        assertEquals(4.7, product.rating!!, 0.001)
        assertEquals(50, product.reviewCount)
        assertEquals(120, product.loveCount)
    }

    @Test
    fun `test SupabaseProductDataSource fails gracefully on network error`() = runBlocking {
        val failingApi = object : SupabaseProductApi {
            override suspend fun getActiveProducts(
                apiKey: String,
                authHeader: String,
                isActive: String,
                order: String,
                limit: Int,
                offset: Int
            ): List<SupabaseProductDto> {
                throw RuntimeException("Network unreachable")
            }
        }

        val dataSource = SupabaseProductDataSource(api = failingApi, apiKey = "test-key")
        val result = dataSource.fetchProducts()

        assertTrue(result.isFailure)
    }

    @Test
    fun `test SupabaseProductDataSource handles pagination correctly`() = runBlocking {
        val page1 = (1..1000).map { i ->
            SupabaseProductDto(id = "id-$i", name = "Product $i", brand = "Brand A", price = 1000.0)
        }
        val page2 = (1001..1500).map { i ->
            SupabaseProductDto(id = "id-$i", name = "Product $i", brand = "Brand B", price = 1500.0)
        }

        val mockApi = object : SupabaseProductApi {
            override suspend fun getActiveProducts(
                apiKey: String,
                authHeader: String,
                isActive: String,
                order: String,
                limit: Int,
                offset: Int
            ): List<SupabaseProductDto> {
                return if (offset == 0) page1 else page2
            }
        }

        val dataSource = SupabaseProductDataSource(api = mockApi, apiKey = "test-key")
        val result = dataSource.fetchProducts()

        assertTrue(result.isSuccess)
        val loaded = result.getOrNull() ?: emptyList()
        assertEquals(1500, loaded.size)
        assertEquals(1500, dataSource.getProducts().size)
        assertEquals("Product 1", dataSource.getProductById("id-1")?.name)
        assertEquals("Product 1500", dataSource.getProductById("id-1500")?.name)
    }

    @Test
    fun `test DefaultProductRepository refreshCatalog updates state`() = runBlocking {
        val testProducts = listOf(
            SupabaseProductDto(id = "p-101", name = "Silk Scarf", brand = "Gucci", price = 25000.0)
        )
        val mockApi = object : SupabaseProductApi {
            override suspend fun getActiveProducts(
                apiKey: String,
                authHeader: String,
                isActive: String,
                order: String,
                limit: Int,
                offset: Int
            ): List<SupabaseProductDto> = testProducts
        }

        val dataSource = SupabaseProductDataSource(api = mockApi, apiKey = "test-key")
        val repository = DefaultProductRepository(dataSource = dataSource)

        val refreshResult = repository.refreshCatalog()
        assertTrue(refreshResult.isSuccess)
        assertEquals(1, repository.getProducts().size)
        assertEquals("Silk Scarf", repository.getProductById("p-101")?.name)
    }

    @Test
    fun `test Curated HeroLook contains distinct piece and worn model images matching product`() {
        val repository = DefaultProductRepository()
        val heroLooks = repository.getHeroLooks()

        assertTrue("Hero looks must not be empty", heroLooks.isNotEmpty())

        for (look in heroLooks) {
            val product = repository.getProductById(look.productId)
            assertNotNull("Product for hero look ${look.id} must exist", product)
            assertNotNull("Hero look ${look.id} must have a hanger/piece image", look.hangerImage)
            assertNotNull("Hero look ${look.id} must have a worn model image", look.wornImage)
            assertNotEquals("Hanger and worn images must be distinct", look.hangerImage, look.wornImage)
            
            // Verify hero_1 specifically points to approved coat piece and worn model
            if (look.id == "hero_1") {
                assertEquals("1", look.productId)
                assertTrue("Hanger image must be piece view", look.hangerImage!!.contains("1572804013309-59a88b7e92f1"))
                assertTrue("Worn image must be approved model view", look.wornImage!!.contains("1515886657613-9f3515b0c78f"))
            }
        }
    }

    @Test
    fun `test Supabase catalog integration preserves separate remote catalog from HeroLooks`() = runBlocking {
        val remoteProducts = listOf(
            SupabaseProductDto(id = "remote-1", name = "Supabase Dress", brand = "Zara", price = 3999.0)
        )
        val mockApi = object : SupabaseProductApi {
            override suspend fun getActiveProducts(
                apiKey: String,
                authHeader: String,
                isActive: String,
                order: String,
                limit: Int,
                offset: Int
            ): List<SupabaseProductDto> = remoteProducts
        }

        val dataSource = SupabaseProductDataSource(api = mockApi, apiKey = "test-key")
        val result = dataSource.fetchProducts()
        assertTrue(result.isSuccess)

        // Real catalog products come from Supabase
        assertEquals(1, dataSource.getProducts().size)
        assertEquals("Supabase Dress", dataSource.getProducts().first().name)
        
        // Curated HeroLooks remain intact and isolated from remote catalog mutations
        val heroLooks = dataSource.getHeroLooks()
        assertTrue(heroLooks.isNotEmpty())
        assertEquals("hero_1", heroLooks.first().id)
    }
}
