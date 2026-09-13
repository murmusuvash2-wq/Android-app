package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.PeekRevealCard
import com.example.data.model.Product
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PeekRevealTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPeekReveal_singleImage_noCounter() {
        val product = Product(
            id = "1",
            name = "Test Product",
            merchant = "Test Brand",
            price = 100.0,
            
            productImages = listOf("https://example.com/image.jpg")
        )

        composeTestRule.setContent {
            PeekRevealCard(
                product = product,
                onClose = {},
                onTryOn = {},
                onBuy = {},
                onToggleFavourite = {}
            )
        }

        // Should not see the counter
        composeTestRule.onNodeWithText("1 / 1").assertDoesNotExist()
        
        // Should see Try On and Buy
        composeTestRule.onNodeWithText("Try On").assertIsDisplayed()
        composeTestRule.onNodeWithText("Buy ↗").assertIsDisplayed()
        
        // Style Tip should not exist
        composeTestRule.onNodeWithText("STYLE TIP:").assertDoesNotExist()
    }

    @Test
    fun testPeekReveal_multipleImages_showsCounter() {
        val product = Product(
            id = "2",
            name = "Test Product 2",
            merchant = "Test Brand",
            price = 200.0,
            
            productImages = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg", "https://example.com/image3.jpg")
        )

        composeTestRule.setContent {
            PeekRevealCard(
                product = product,
                onClose = {},
                onTryOn = {},
                onBuy = {},
                onToggleFavourite = {}
            )
        }

        // Should see the counter for first page
        composeTestRule.onNodeWithText("1 / 3").assertIsDisplayed()
    }
}
