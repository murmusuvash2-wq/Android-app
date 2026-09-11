package com.example

import com.example.credit.repository.CreditRepositoryProvider

import com.example.data.datasource.MockProductDataSource
import com.example.data.datasource.ProductDataSource
import com.example.data.model.Product
import com.example.data.repository.DefaultProductRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ProductRepositoryProvider
import com.example.ui.DiscoverTab
import com.example.ui.OnMeStyleRepository
import org.junit.After
import org.junit.Assert.*
import com.example.credit.repository.FakeCreditRepository
import org.junit.Before
import org.junit.Test

class ProductRepositoryTest {

    private lateinit var repository: ProductRepository

    @Before
    fun setUp() {
        com.example.credit.repository.CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        ProductRepositoryProvider.reset()
        repository = ProductRepository.get()
        OnMeStyleRepository.resetForTesting()
    }

    @After
    fun tearDown() {
        ProductRepositoryProvider.reset()
        OnMeStyleRepository.resetForTesting()
    }

    @Test
    fun testGetProducts_returnsExpectedCatalog() {
        val products = repository.getProducts()
        assertEquals(6, products.size)
        assertEquals(listOf("1", "2", "3", "4", "5", "6"), products.map { it.id })
    }

    @Test
    fun testGetProductById_resolvesDirectId() {
        val product1 = repository.getProductById("1")
        assertNotNull(product1)
        assertEquals("1", product1!!.id)
        assertEquals("Oversized Cashmere Trench", product1.name)
        assertEquals("ZARA", product1.brand)
        assertEquals("ZARA", product1.merchant) // Backward-compatible getter
        assertEquals(12999.0, product1.price, 0.001)
        assertFalse(product1.productImages.isEmpty())
        assertEquals(product1.productImages.first(), product1.primaryImageUrl)
        assertEquals(product1.primaryImageUrl, product1.imageUrl)
    }

    @Test
    fun testGetProductById_resolvesLegacyAlias() {
        // "p1" through "p6" must resolve to products "1" through "6"
        val p1 = repository.getProductById("p1")
        assertNotNull(p1)
        assertEquals("1", p1!!.id)
        assertEquals("Oversized Cashmere Trench", p1.name)

        val p3 = repository.getProductById("p3")
        assertNotNull(p3)
        assertEquals("3", p3!!.id)
        assertEquals("Minimalist Linen Blazer", p3.name)

        val p6 = repository.getProductById("p6")
        assertNotNull(p6)
        assertEquals("6", p6!!.id)
        assertEquals("Pastel Co-ord Loungewear", p6.name)
    }

    @Test
    fun testGetProductById_unknownId_returnsNullSafely() {
        val unknown = repository.getProductById("non_existent_id")
        assertNull(unknown)
    }

    @Test
    fun testGetTrendingProducts_returnsHomeShowcaseItems() {
        val trending = repository.getTrendingProducts()
        assertEquals(4, trending.size)
        assertEquals("home_trending_1", trending[0].id)
        assertEquals("Linen Day Dress", trending[0].name)
        assertEquals("URBANIC", trending[0].brand)
        assertEquals(2499.0, trending[0].price, 0.001)

        // Verify alias for trending
        val trending1ByAlias = repository.getProductById("trending_1")
        assertNotNull(trending1ByAlias)
        assertEquals("home_trending_1", trending1ByAlias!!.id)
        assertEquals("Linen Day Dress", trending1ByAlias.name)
    }

    @Test
    fun testSearchProducts_filtersByNameBrandAndDescription() {
        // By Name
        val cashmeres = repository.searchProducts("cashmere")
        assertEquals(1, cashmeres.size)
        assertEquals("1", cashmeres.first().id)

        // By Brand
        val zaraProducts = repository.searchProducts("zara")
        assertEquals(2, zaraProducts.size)
        assertEquals(listOf("1", "4"), zaraProducts.map { it.id })

        // By Description keyword
        val recycled = repository.searchProducts("recycled")
        assertEquals(1, recycled.size)
        assertEquals("2", recycled.first().id)

        // Empty query returns all products
        val all = repository.searchProducts("")
        assertEquals(6, all.size)
    }

    @Test
    fun testGetProductsForTab_delegatesTabFilteringCorrectly() {
        val justIn = repository.getProductsForTab(DiscoverTab.JUST_IN)
        assertEquals(6, justIn.size)
        assertEquals("6", justIn.first().id) // Reverse catalog order

        val bestSellers = repository.getProductsForTab(DiscoverTab.BEST_SELLERS)
        assertEquals(6, bestSellers.size)
        assertEquals("3", bestSellers.first().id) // Highest review count
    }

    @Test
    fun testProductImmutabilityAndSeparationFromOnMeStyleRepository() {
        // Catalog products from repository are read-only catalog domain objects
        val baseProduct = repository.getProductById("1")!!
        assertFalse(baseProduct.isFavourite)

        // Favouriting a product in OnMeStyleRepository modifies user state, not the catalog repository data source
        OnMeStyleRepository.setFavourite(
            productId = "1",
            isFav = true,
            productName = baseProduct.name,
            merchant = baseProduct.brand,
            price = baseProduct.price,
            imageUrl = baseProduct.primaryImageUrl
        )

        // Repository remains unchanged and catalog data is immutable
        val refreshedBase = repository.getProductById("1")!!
        assertFalse("Catalog repository product remains immutable", refreshedBase.isFavourite)

        // UI layers compose the two sources cleanly
        assertTrue("User state reflects in OnMeStyleRepository", OnMeStyleRepository.isFavourite("1"))
        val uiProduct = refreshedBase.copy(isFavourite = OnMeStyleRepository.isFavourite(refreshedBase.id))
        assertTrue("Composed UI model reflects user favourite", uiProduct.isFavourite)
    }

    @Test
    fun testLegacyIdAliasingInOnMeStyleRepository() {
        // Favouriting via "p1" must be recognized when checked with "1"
        OnMeStyleRepository.setFavourite(
            productId = "p1",
            isFav = true,
            productName = "Oversized Cashmere Trench",
            merchant = "ZARA",
            price = 12999.0
        )
        assertTrue(OnMeStyleRepository.isFavourite("p1"))
        assertTrue("Checking with canonical id '1' must return true", OnMeStyleRepository.isFavourite("1"))
        assertTrue("Price tracking must also be enabled by alias", OnMeStyleRepository.isPriceTracked("1"))

        // Unfavouriting via "1" clears both
        OnMeStyleRepository.setFavourite(productId = "1", isFav = false)
        assertFalse(OnMeStyleRepository.isFavourite("1"))
        assertFalse(OnMeStyleRepository.isFavourite("p1"))
    }

    @Test
    fun testCustomDataSourceInjectionForTesting() {
        val testProduct = Product(
            id = "custom_test_99",
            name = "Test Custom Silk Shirt",
            brand = "CUSTOM_BRAND",
            productImages = listOf("https://example.com/custom.jpg"),
            price = 4999.0
        )

        val customDataSource = object : ProductDataSource {
            override fun getProducts(): List<Product> = listOf(testProduct)
            override fun getProductById(id: String): Product? = if (id == testProduct.id) testProduct else null
            override fun getTrendingProducts(): List<Product> = listOf(testProduct)
        }

        ProductRepositoryProvider.setForTesting(DefaultProductRepository(customDataSource))
        val customRepo = ProductRepository.get()

        assertEquals(1, customRepo.getProducts().size)
        assertEquals("custom_test_99", customRepo.getProducts().first().id)
        assertEquals("Test Custom Silk Shirt", customRepo.getProductById("custom_test_99")?.name)

        // Reset restores default repository
        ProductRepositoryProvider.reset()
        assertEquals(6, ProductRepository.get().getProducts().size)
    }
}
