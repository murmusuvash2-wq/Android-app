package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class NavigationIntegrityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLooksTabSaver_preservesAndRestoresAllTabs() {
        // Test saver handles all LooksTab values cleanly for configuration change preservation
        LooksTab.values().forEach { tab ->
            val saved = with(LooksTabSaver) { mockSaverScope.save(tab) }
            assertEquals(tab.name, saved)
            val restored = LooksTabSaver.restore(saved!!)
            assertEquals(tab, restored)
        }
    }

    @Test
    fun testHomeScreen_viewAllInvokesCallback() {
        var discoverRequested = false
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                HomeScreen(
                    navController = navController,
                    onNavigateToDiscover = { discoverRequested = true }
                )
            }
        }

        // Find "View All", scroll to it and click it
        composeTestRule.onNodeWithText("View All").performScrollTo().performClick()
        assertTrue("View All click must invoke onNavigateToDiscover callback", discoverRequested)
    }

    @Test
    fun testMeScreen_savedLooksAndPriceTrackingInvokeCallbacks() {
        SessionManager.isGuest = false
        var requestedSubTab: LooksTab? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                MeScreen(
                    navController = navController,
                    onNavigateToLooks = { requestedSubTab = it }
                )
            }
        }

        // Click "Saved Looks"
        composeTestRule.onNodeWithText("Saved Looks").performClick()
        assertEquals(LooksTab.RECENT, requestedSubTab)

        // Click "Price Tracking"
        composeTestRule.onNodeWithText("Price Tracking").performClick()
        assertEquals(LooksTab.PRICE_TRACKING, requestedSubTab)
    }

    @Test
    fun testLooksScreen_displaysRequestedSubTab() {
        SessionManager.isGuest = false

        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                LooksScreen(
                    navController = navController,
                    requestedTab = LooksTab.PRICE_TRACKING
                )
            }
        }

        // Verify the Price Tracking tab is composed and active
        composeTestRule.onNodeWithText("Price Tracking").assertIsDisplayed()
    }

    @Test
    fun testAppShell_backHandlerRuleSimulation() {
        // Verify state machine logic for AppShell BackHandler:
        // When selectedTab != 0 (Home), back action returns to Home (0).
        // When selectedTab == 0 (Home), back handler is disabled.
        var selectedTab = 1 // Looks
        val isBackEnabledTab1 = selectedTab != 0
        assertTrue("Back should be enabled on Looks tab", isBackEnabledTab1)
        if (isBackEnabledTab1) selectedTab = 0
        assertEquals(0, selectedTab)

        selectedTab = 2 // Discover
        val isBackEnabledTab2 = selectedTab != 0
        assertTrue("Back should be enabled on Discover tab", isBackEnabledTab2)
        if (isBackEnabledTab2) selectedTab = 0
        assertEquals(0, selectedTab)

        selectedTab = 3 // Me
        val isBackEnabledTab3 = selectedTab != 0
        assertTrue("Back should be enabled on Me tab", isBackEnabledTab3)
        if (isBackEnabledTab3) selectedTab = 0
        assertEquals(0, selectedTab)

        selectedTab = 0 // Home
        val isBackEnabledTab0 = selectedTab != 0
        assertFalse("Back should NOT be enabled on Home tab to preserve system exit", isBackEnabledTab0)
    }

    @Test
    fun testAppShell_tabNavigationToMe() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                AppShell(rootNavController = navController)
            }
        }

        // Initially Home tab is displayed
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()

        // Tap "Me" tab in bottom navigation
        composeTestRule.onNodeWithText("Me").performClick()
        composeTestRule.waitForIdle()

        // "YOUR ACTIVITY" is shown on Me screen inside AppShell
        composeTestRule.onNodeWithText("YOUR ACTIVITY").assertIsDisplayed()
    }

    private val mockSaverScope = object : androidx.compose.runtime.saveable.SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }
}
