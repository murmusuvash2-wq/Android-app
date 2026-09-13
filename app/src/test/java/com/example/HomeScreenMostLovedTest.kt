package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.test.core.app.ApplicationProvider
import androidx.navigation.compose.rememberNavController
import com.example.ui.HomeScreen
import com.example.ui.SessionManager
import com.example.ui.TiHinStyleRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.data.repository.ProductRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HomeScreenMostLovedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        com.example.credit.repository.CreditRepositoryProvider.init(context)
        SessionManager.resetForTesting(initialGuest = false, context = context)
        TiHinStyleRepository.initSync(context)
        
        // Reset favourites
        val repo = TiHinStyleRepository
        repo.favouriteProductIds.forEach { 
            repo.toggleFavourite(it, synchronous = true, context = context)
        }
    }

    @Test
    fun testHomeScreen_mostLoved_emptyState() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Nothing loved yet").assertExists()
        composeTestRule.onNodeWithText("Tap the heart on a style you love.").assertExists()
    }

    @Test
    fun testHomeScreen_mostLoved_addsAndRemovesReactive() {
        val testProduct = ProductRepository.get().getTrendingProducts().first()

        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        // Empty initially
        composeTestRule.onNodeWithText("Nothing loved yet").assertExists()

        // Toggle favourite
        TiHinStyleRepository.toggleFavourite(
            productId = testProduct.id, 
            synchronous = true, 
            context = context
        )

        // Wait for UI to update
        composeTestRule.waitForIdle()

        // The product should appear (likely twice now: once in trending, once in most loved)
        // Empty state should be gone
        composeTestRule.onNodeWithText("Nothing loved yet").assertDoesNotExist()

        // Toggle again to remove
        TiHinStyleRepository.toggleFavourite(
            productId = testProduct.id, 
            synchronous = true, 
            context = context
        )

        composeTestRule.waitForIdle()

        // Empty state returns
        composeTestRule.onNodeWithText("Nothing loved yet").assertExists()
    }
}
