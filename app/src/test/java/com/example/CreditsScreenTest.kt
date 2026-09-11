package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.ui.CreditsScreen
import com.example.credit.repository.FakeCreditRepository
import com.example.credit.repository.CreditRepositoryProvider
import com.example.ui.SessionManager
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
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
class CreditsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        SessionManager.isGuest = false
        SessionManager.resetForTesting(initialFree = 2, initialPurchased = 0)
        SessionManager.releaseHeldCredit()
    }

    @Test
    fun testCreditsScreen_rendersInitialBalanceAndPacks() {



        composeTestRule.waitUntil(2000) { SessionManager.freeCredits == 2 && SessionManager.purchasedCredits == 0 }
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                CreditsScreen(navController = navController)
            }
        }

        // Top App Bar
        composeTestRule.onNodeWithText("Credits").assertIsDisplayed()
        composeTestRule.onNodeWithTag("credits_back_button").assertIsDisplayed()

        // Balance Hero (default 2 Free Credits)
        composeTestRule.onNodeWithText("Your Credits").assertIsDisplayed()
        composeTestRule.onRoot().printToLog("TEST_TREE")
        println("CREDITS_IS: " + com.example.ui.SessionManager.credits)
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 Free Credits").assertIsDisplayed()
        composeTestRule.onNodeWithText("Free Try-On includes a short ad.").assertIsDisplayed()

        // Buy Credits Section
        composeTestRule.onNodeWithText("Buy Credits").assertExists()
        composeTestRule.onNodeWithText("5 Credits").assertExists()
        composeTestRule.onNodeWithText("₹49").assertExists()
        composeTestRule.onNodeWithText("15 Credits").assertExists()
        composeTestRule.onNodeWithText("₹99").assertExists()
        composeTestRule.onNodeWithText("Most Popular").assertExists()
        composeTestRule.onNodeWithText("Buy 5 Credits").assertExists()
        composeTestRule.onNodeWithText("Buy 15 Credits").assertExists()

        // Premium Section
        composeTestRule.onNodeWithText("TiHin Premium").assertExists()
        composeTestRule.onNodeWithText("₹299").assertExists()
        composeTestRule.onNodeWithText("Get more from TiHin").assertExists()
        composeTestRule.onNodeWithText("Get Premium").assertExists()
        composeTestRule.onNodeWithText("Ad-free experience").assertExists()
        composeTestRule.onNodeWithText("Priority AI processing").assertExists()

        // Credit History
        composeTestRule.onNodeWithText("Credit History").assertExists()
        composeTestRule.onNodeWithText("View all →").assertExists()
        composeTestRule.onNodeWithText("Welcome credits").assertExists()
        composeTestRule.onNodeWithText("+2 Credits").assertExists()
        composeTestRule.onNodeWithText("Try-On").assertExists()
        composeTestRule.onNodeWithText("−1 Credit").assertExists()
    

}

    @Test
    fun testCreditsScreen_rendersCombinedFreeAndPurchasedState() {



        SessionManager.resetForTesting(initialFree = 2, initialPurchased = 5)

        composeTestRule.waitUntil(2000) { SessionManager.freeCredits == 2 && SessionManager.purchasedCredits == 5 }
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                CreditsScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("7").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 Free · 5 Purchased").assertIsDisplayed()
        composeTestRule.onNodeWithText("Purchased credits are ad-free. Free Try-On includes a short ad.").assertIsDisplayed()
    

}

    @Test
    fun testCreditsScreen_rendersPurchasedOnlyState() {



        SessionManager.resetForTesting(initialFree = 0, initialPurchased = 5)

        composeTestRule.waitUntil(2000) { SessionManager.freeCredits == 0 && SessionManager.purchasedCredits == 5 }
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                CreditsScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("5").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 Purchased Credits").assertIsDisplayed()
        // Subtitle exists in hero and section header
        composeTestRule.onAllNodesWithText("Purchased credits are ad-free.")[0].assertExists()
    

}

    @Test
    fun testCreditsScreen_rendersZeroBalanceState() {



        SessionManager.resetForTesting(initialFree = 0, initialPurchased = 0)

        composeTestRule.waitUntil(2000) { SessionManager.freeCredits == 0 && SessionManager.purchasedCredits == 0 }
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                CreditsScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("0").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 Credits remaining").assertIsDisplayed()
    

}

    @Test
    fun testCreditsScreen_tappingBuyPackAddsCredits() {



        SessionManager.resetForTesting(initialFree = 2, initialPurchased = 0)

        composeTestRule.waitUntil(2000) { SessionManager.freeCredits == 2 && SessionManager.purchasedCredits == 0 }
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                CreditsScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithTag("buy_button_pack_5").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(5, SessionManager.purchasedCredits)
        assertEquals(7, SessionManager.credits)
    

}
}
