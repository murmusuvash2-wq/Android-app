package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.ProductRepositoryProvider
import com.example.ui.HomeScreen
import com.example.ui.SessionManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class HomeScreenHeroTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        SessionManager.init(context)
        SessionManager.isGuest = true
        ProductRepositoryProvider.reset()
    }

    @Test
    fun testHero_rendersTwoStatesWhenAvailable() {
        composeTestRule.setContent {
            HomeScreen(navController = rememberNavController())
        }
        
        // Product 1 (Oversized Cashmere Trench) is first in HERO_LOOKS
        // It has 3 images, so it should resolve both Outfit product view and Fitted view
        composeTestRule.onNodeWithText("Oversized Cashmere Trench").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Outfit product view").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Fitted view").assertIsDisplayed()
    }

    @Test
    fun testHero_resolvesProductMetadata_doesNotInventData() {
        composeTestRule.setContent {
            HomeScreen(navController = rememberNavController())
        }
        
        // It should resolve the actual canonical product metadata.
        // Product 1 has price 12999.0
        composeTestRule.onNodeWithText("₹12,999").assertIsDisplayed()
    }
    
    @Test
    fun testHero_excludesItemsWithoutTwoValidImages() {
        composeTestRule.setContent {
            HomeScreen(navController = rememberNavController())
        }
        
        // Structured Oxford & Trousers only has 1 image in CATALOG_PRODUCTS.
        // It must be excluded from the Hero carousel.
        composeTestRule.onNodeWithText("Structured Oxford & Trousers").assertDoesNotExist()
        
        // Ensure "Oversized Cashmere Trench" which has multiple images STILL appears
        composeTestRule.onNodeWithText("Oversized Cashmere Trench").assertIsDisplayed()
    }
}
