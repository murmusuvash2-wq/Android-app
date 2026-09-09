package com.example

import com.example.ui.DiscoverProduct
import com.example.ui.DiscoverTab
import com.example.ui.MOCK_DISCOVER_PRODUCTS
import com.example.ui.OnMeStyleRepository
import com.example.ui.getProductsForTab
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DiscoverTabFilteringTest {

    @Before
    fun setUp() {
        OnMeStyleRepository.resetForTesting()
    }

    @Test
    fun testTrendingNow_returnsExpectedProductsAndOrder() {
        // Trending tab returns the baseline curated mock products order
        val trendingProducts = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.TRENDING)
        assertEquals(MOCK_DISCOVER_PRODUCTS.size, trendingProducts.size)
        assertEquals(MOCK_DISCOVER_PRODUCTS.map { it.id }, trendingProducts.map { it.id })
        assertEquals("1", trendingProducts.first().id)
        assertEquals("6", trendingProducts.last().id)
    }

    @Test
    fun testMostLoved_ordersFavouritedFirstThenHighestReviewCount() {
        // Initially no favourites: sorted by reviewCount descending
        // MOCK_DISCOVER_PRODUCTS review counts:
        // id 3: 210
        // id 5: 148
        // id 1: 124
        // id 2: 89
        // id 4: 67
        // id 6: null (0)
        val initialMostLoved = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.MOST_LOVED)
        assertEquals(listOf("3", "5", "1", "2", "4", "6"), initialMostLoved.map { it.id })

        // When user favourites product "4", it must jump to the very top in Most Loved
        val productsWithFav = MOCK_DISCOVER_PRODUCTS.map {
            if (it.id == "4") it.copy(isFavourite = true) else it
        }
        val updatedMostLoved = getProductsForTab(productsWithFav, DiscoverTab.MOST_LOVED)
        assertEquals("4", updatedMostLoved.first().id)
        assertTrue(updatedMostLoved.first().isFavourite)
    }

    @Test
    fun testBestSellers_ordersByReviewVolumeDescending() {
        // Best sellers sorts strictly by real customer review feedback volume descending
        val bestSellers = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.BEST_SELLERS)
        assertEquals(MOCK_DISCOVER_PRODUCTS.size, bestSellers.size)
        assertEquals(listOf("3", "5", "1", "2", "4", "6"), bestSellers.map { it.id })
        assertEquals("Minimalist Linen Blazer", bestSellers.first().name)
    }

    @Test
    fun testJustIn_returnsNewArrivalsInReverseCatalogOrder() {
        // Just In returns reverse chronological catalog order (newest arrival first)
        val justIn = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.JUST_IN)
        assertEquals(MOCK_DISCOVER_PRODUCTS.size, justIn.size)
        assertEquals(MOCK_DISCOVER_PRODUCTS.reversed().map { it.id }, justIn.map { it.id })
        assertEquals("6", justIn.first().id)
        assertEquals("Pastel Co-ord Loungewear", justIn.first().name)
        assertEquals("1", justIn.last().id)
    }

    @Test
    fun testSearchPlusTrending_filtersWithinTrendingOrder() {
        val query = "zara"
        val trending = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.TRENDING)
        val filtered = trending.filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(2, filtered.size)
        assertEquals(listOf("1", "4"), filtered.map { it.id })
    }

    @Test
    fun testSearchPlusMostLoved_filtersWithinMostLovedOrder() {
        // Search for "zara" in Most Loved:
        // Product 1 (ZARA, 124 reviews) and Product 4 (ZARA, 67 reviews)
        // In Most Loved without favourites, Product 1 (124) ranks before Product 4 (67)
        val query = "zara"
        val mostLoved = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.MOST_LOVED)
        val filtered = mostLoved.filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(2, filtered.size)
        assertEquals(listOf("1", "4"), filtered.map { it.id })

        // If product 4 is favourited, it should appear before product 1
        val productsWithFav = MOCK_DISCOVER_PRODUCTS.map {
            if (it.id == "4") it.copy(isFavourite = true) else it
        }
        val mostLovedWithFav = getProductsForTab(productsWithFav, DiscoverTab.MOST_LOVED)
        val filteredWithFav = mostLovedWithFav.filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(listOf("4", "1"), filteredWithFav.map { it.id })
    }

    @Test
    fun testSearchPlusBestSellers_filtersWithinBestSellersOrder() {
        val query = "dress"
        val bestSellers = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.BEST_SELLERS)
        val filtered = bestSellers.filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(1, filtered.size)
        assertEquals("5", filtered.first().id)
        assertEquals("Emerald Satin Maxi Dress", filtered.first().name)
    }

    @Test
    fun testSearchPlusJustIn_filtersWithinJustInOrder() {
        val query = "zara"
        val justIn = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.JUST_IN)
        val filtered = justIn.filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(2, filtered.size)
        // In Just In (reverse order), product 4 appears before product 1
        assertEquals(listOf("4", "1"), filtered.map { it.id })
    }

    @Test
    fun testClearingSearch_preservesSelectedTabDataset() {
        var query = "cashmere"
        val tab = DiscoverTab.JUST_IN

        // With search query
        val tabProducts = getProductsForTab(MOCK_DISCOVER_PRODUCTS, tab)
        val searchFiltered = tabProducts.filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(1, searchFiltered.size)

        // Clear search: query becomes empty
        query = ""
        val restored = if (query.isBlank()) tabProducts else tabProducts.filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        // Must restore all Just In products in reverse catalog order
        assertEquals(MOCK_DISCOVER_PRODUCTS.size, restored.size)
        assertEquals("6", restored.first().id)
    }

    @Test
    fun testSwitchingTabs_preservesSearchQuery() {
        val query = "dress"

        // Search query "dress" applied to Trending
        val trendingResults = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.TRENDING).filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(1, trendingResults.size)
        assertEquals("5", trendingResults.first().id)

        // Switch to Just In with same query
        val justInResults = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.JUST_IN).filter {
            it.name.contains(query, ignoreCase = true) || it.merchant.contains(query, ignoreCase = true)
        }
        assertEquals(1, justInResults.size)
        assertEquals("5", justInResults.first().id)
    }

    @Test
    fun testEmptyTabState_handledSafely() {
        val emptyList = emptyList<DiscoverProduct>()
        DiscoverTab.values().forEach { tab ->
            val result = getProductsForTab(emptyList, tab)
            assertNotNull(result)
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun testProductSelectionFromTab_retainsCorrectProduct() {
        // Tab Just In has item 6 at index 0
        val justIn = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.JUST_IN)
        val selectedFromJustIn = justIn[0]
        assertEquals("6", selectedFromJustIn.id)
        assertEquals("Pastel Co-ord Loungewear", selectedFromJustIn.name)
        assertEquals("ASOS", selectedFromJustIn.merchant)

        // Tab Best Sellers has item 3 at index 0
        val bestSellers = getProductsForTab(MOCK_DISCOVER_PRODUCTS, DiscoverTab.BEST_SELLERS)
        val selectedFromBestSellers = bestSellers[0]
        assertEquals("3", selectedFromBestSellers.id)
        assertEquals("Minimalist Linen Blazer", selectedFromBestSellers.name)
        assertEquals("MANGO", selectedFromBestSellers.merchant)
    }

    @Test
    fun testFavouriteState_notResetByTabSwitching() {
        // Toggle favourite in OnMeStyleRepository
        OnMeStyleRepository.toggleFavourite(
            productId = "1",
            productName = "Oversized Cashmere Trench",
            merchant = "ZARA",
            price = 12999.0,
            imageUrl = "https://example.com/1.jpg"
        )
        assertTrue(OnMeStyleRepository.isFavourite("1"))

        // Reconstitute products mapped with repository favourite state
        val repoProducts = MOCK_DISCOVER_PRODUCTS.map {
            it.copy(isFavourite = OnMeStyleRepository.isFavourite(it.id))
        }

        // Verify favourite is preserved across all 4 tab queries
        DiscoverTab.values().forEach { tab ->
            val tabFiltered = getProductsForTab(repoProducts, tab)
            val p1 = tabFiltered.firstOrNull { it.id == "1" }
            assertNotNull("Product 1 must be present in tab $tab", p1)
            assertTrue("Product 1 favourite must be preserved in tab $tab", p1!!.isFavourite)
        }
    }
}
