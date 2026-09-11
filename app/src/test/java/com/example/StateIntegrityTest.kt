package com.example

import com.example.credit.repository.CreditRepositoryProvider

import com.example.ui.OnMeStyleRepository
import com.example.ui.TrackedProduct
import com.example.ui.TryOnResult
import org.junit.Assert.*
import com.example.credit.repository.FakeCreditRepository
import org.junit.Before
import org.junit.Test

class StateIntegrityTest {

    @Before
    fun setUp() {
        com.example.credit.repository.CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        OnMeStyleRepository.resetForTesting()
    }

    @Test
    fun testUnifiedFavourites_toggleAcrossViews() {
        assertFalse(OnMeStyleRepository.isFavourite("p1"))

        // Favourite p1
        val isNowFav = OnMeStyleRepository.toggleFavourite(
            productId = "p1",
            productName = "Oversized Cashmere Trench",
            merchant = "ZARA",
            price = 12999.0,
            imageUrl = "https://example.com/p1.jpg"
        )
        assertTrue(isNowFav)
        assertTrue(OnMeStyleRepository.isFavourite("p1"))

        // Unfavourite p1
        val isNowUnfav = OnMeStyleRepository.toggleFavourite("p1")
        assertFalse(isNowUnfav)
        assertFalse(OnMeStyleRepository.isFavourite("p1"))
    }

    @Test
    fun testLoveEnablesPriceTracking_andManualUntrackPreservesFavourite() {
        // Initially product "p2" is not in favourites or price tracking
        assertFalse(OnMeStyleRepository.isFavourite("p2"))
        assertFalse(OnMeStyleRepository.isPriceTracked("p2"))

        // Favouriting product "p2" must automatically enable Price Tracking
        OnMeStyleRepository.toggleFavourite(
            productId = "p2",
            productName = "Tailored Wool Overcoat",
            merchant = "H&M",
            price = 8999.0,
            imageUrl = "https://example.com/p2.jpg"
        )
        assertTrue("Product must be in favourites", OnMeStyleRepository.isFavourite("p2"))
        assertTrue("Product must be automatically price tracked when favourited", OnMeStyleRepository.isPriceTracked("p2"))

        // Manually turning Price Tracking OFF later must NOT remove the favourite
        val newTrackedState = OnMeStyleRepository.togglePriceTracking("p2")
        assertFalse("Price tracking should now be off", newTrackedState)
        assertFalse("Price tracking query must return false", OnMeStyleRepository.isPriceTracked("p2"))
        assertTrue("Product MUST remain in favourites after untracking price", OnMeStyleRepository.isFavourite("p2"))
    }

    @Test
    fun testSaveTryOnResult_appearsAtTopAndIsIdempotent() {
        assertEquals(0, OnMeStyleRepository.savedResults.size)

        val result1 = TryOnResult(
            id = "res_1",
            userPhoto = "photo_1",
            outfitImage = "outfit_1",
            resultImage = "result_1",
            createdAt = "Just now",
            isFavourite = false,
            productId = "prod_1",
            productName = "Dress 1",
            productBrand = "ZARA",
            productPrice = 1999.0,
            cardHeight = 240
        )

        val result2 = TryOnResult(
            id = "res_2",
            userPhoto = "photo_2",
            outfitImage = "outfit_2",
            resultImage = "result_2",
            createdAt = "Just now",
            isFavourite = true,
            productId = "prod_2",
            productName = "Coat 2",
            productBrand = "MANGO",
            productPrice = 4999.0,
            cardHeight = 250
        )

        // Save first result
        OnMeStyleRepository.saveResult(result1)
        assertEquals(1, OnMeStyleRepository.savedResults.size)
        assertEquals("res_1", OnMeStyleRepository.savedResults.first().id)

        // Save second result: must appear at top (index 0)
        OnMeStyleRepository.saveResult(result2)
        assertEquals(2, OnMeStyleRepository.savedResults.size)
        assertEquals("res_2", OnMeStyleRepository.savedResults[0].id)
        assertEquals("res_1", OnMeStyleRepository.savedResults[1].id)

        // Saving duplicate / updated result with same ID must be idempotent and not create duplicate entries
        OnMeStyleRepository.saveResult(result1)
        assertEquals(2, OnMeStyleRepository.savedResults.size)
        assertEquals("res_1", OnMeStyleRepository.savedResults[0].id)
        assertEquals("res_2", OnMeStyleRepository.savedResults[1].id)
    }

    @Test
    fun testPriceTracking_addsToCustomTrackedAndReflectsInActiveTrackedList() {
        val fixtures = listOf(
            TrackedProduct(
                id = "p1",
                productImage = "img_p1",
                productName = "Trench",
                merchant = "ZARA",
                currentPrice = 12999.0,
                productUrl = "",
                trackedAt = "1w ago"
            ),
            TrackedProduct(
                id = "p3",
                productImage = "img_p3",
                productName = "Blazer",
                merchant = "MANGO",
                currentPrice = 6590.0,
                productUrl = "",
                trackedAt = "2w ago"
            )
        )

        // Initially p1 and p3 are tracked in baseline fixtures
        val initialTracked = OnMeStyleRepository.getActiveTrackedProducts(fixtures)
        assertEquals(2, initialTracked.size)

        // Untrack p1
        OnMeStyleRepository.togglePriceTracking("p1")
        val afterUntrackP1 = OnMeStyleRepository.getActiveTrackedProducts(fixtures)
        assertEquals(1, afterUntrackP1.size)
        assertEquals("p3", afterUntrackP1.first().id)

        // Track a brand new product from Result or Discover
        OnMeStyleRepository.setPriceTracking(
            productId = "prod_custom",
            enabled = true,
            productName = "New Summer Outfit",
            merchant = "URBANIC",
            price = 2499.0,
            imageUrl = "https://example.com/summer.jpg"
        )

        val updatedTracked = OnMeStyleRepository.getActiveTrackedProducts(fixtures)
        assertTrue(updatedTracked.any { it.id == "prod_custom" })
        assertEquals("New Summer Outfit", updatedTracked.first { it.id == "prod_custom" }.productName)
    }
}
