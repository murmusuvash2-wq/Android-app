package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.example.ui.OnboardingNativePage
import com.example.ui.OnboardingPageData
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOnboardingScreen_threeScreensSequence_tryLoveBuy() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                OnboardingScreen(navController = navController)
            }
        }

        // Screen 1: "Try."
        // Full editorial custom artwork is displayed as the primary visual content
        composeTestRule.onNodeWithTag("onboarding_custom_image_try").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Try. See it on you before you buy.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_skip_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_next_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()

        // Ensure no duplicate app-generated heading or footer exists outside the custom artwork
        composeTestRule.onNodeWithTag("onboarding_fallback_title_try").assertDoesNotExist()
        composeTestRule.onNodeWithTag("onboarding_fallback_subtitle_try").assertDoesNotExist()
        composeTestRule.onNodeWithText("TiHin • Your Style, Your Way").assertDoesNotExist()
        composeTestRule.onNodeWithText("TiHin · Your Style, Your Way").assertDoesNotExist()

        // Tap Next to advance to Screen 2: "Love."
        composeTestRule.onNodeWithTag("onboarding_next_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("onboarding_custom_image_love").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Love. Find your perfect look with confidence.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_fallback_title_love").assertDoesNotExist()
        composeTestRule.onNodeWithText("Next").assertIsDisplayed()

        // Tap Next to advance to Screen 3: "Buy."
        composeTestRule.onNodeWithTag("onboarding_next_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("onboarding_custom_image_buy").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Buy. Shop your favourite styles in just a tap.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_fallback_title_buy").assertDoesNotExist()
        // On the final 3rd screen, the button transitions to "Get Started"
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed()

        // Ensure fallback cards and legacy text are absent when custom artwork exists
        composeTestRule.onNodeWithTag("onboarding_fallback_card_try").assertDoesNotExist()
        composeTestRule.onNodeWithTag("onboarding_fallback_card_love").assertDoesNotExist()
        composeTestRule.onNodeWithTag("onboarding_fallback_card_buy").assertDoesNotExist()
        composeTestRule.onNodeWithText("AI Virtual Try-On").assertDoesNotExist()
        composeTestRule.onNodeWithText("My Favourite Looks").assertDoesNotExist()
        composeTestRule.onNodeWithText("Floral Midi Dress").assertDoesNotExist()
    }

    @Test
    fun testOnboardingScreen_skipButton_opensAuthBottomSheet() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                OnboardingScreen(navController = navController)
            }
        }

        // Skip button is present
        composeTestRule.onNodeWithTag("onboarding_skip_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_skip_button").performClick()
        composeTestRule.waitForIdle()

        // Auth options are displayed
        composeTestRule.onNodeWithText("Continue as Guest").assertIsDisplayed()
    }

    @Test
    fun testOnboardingFallback_whenCustomAssetUnavailable_rendersFallbackUI() {
        // When drawableRes is 0 (custom artwork genuinely unavailable), fallback UI renders
        composeTestRule.setContent {
            MyApplicationTheme {
                OnboardingNativePage(
                    item = OnboardingPageData(
                        title = "Try.",
                        subtitle = "See it on you before you buy.",
                        tag = "try",
                        drawableRes = 0
                    )
                )
            }
        }

        composeTestRule.onNodeWithTag("onboarding_fallback_title_try").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_fallback_subtitle_try").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_fallback_card_try").assertIsDisplayed()
        composeTestRule.onNodeWithTag("onboarding_custom_image_try").assertDoesNotExist()
    }
}
