package com.example

import com.example.credit.repository.CreditRepositoryProvider
import com.example.credit.repository.FakeCreditRepository
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.share.ui.ShareFlowManager
import com.example.share.ui.SharedProductErrorScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class SharedProductErrorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        ShareFlowManager.resetForTesting()
    }

    @Test
    fun testSharedProductErrorScreen_rendersAndOffersActions() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                SharedProductErrorScreen(navController = navController)
            }
        }
        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.onNodeWithTag("shared_product_error_screen").assertExists()
        composeTestRule.onNodeWithTag("shared_product_error_title").assertExists()
        composeTestRule.onNodeWithText("We couldn't recognize this product").assertExists()
        composeTestRule.onNodeWithTag("try_again_button").assertExists()
        composeTestRule.onNodeWithTag("choose_product_button").assertExists()
    }
}
