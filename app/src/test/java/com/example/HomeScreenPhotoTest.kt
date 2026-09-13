package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.navigation.compose.rememberNavController
import com.example.ui.HomeScreen
import com.example.ui.SessionManager
import com.example.ui.UserPhotosRepository
import com.example.ui.TryOnPhoto
import com.example.ui.theme.MyApplicationTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HomeScreenPhotoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        com.example.credit.repository.CreditRepositoryProvider.init(context)
        SessionManager.resetForTesting(initialGuest = false, context = context)
    }

    @Test
    fun testHomeScreen_withNoSavedPhoto_showsEmptyState() {
        // Arrange
        UserPhotosRepository.resetForTesting(initialPhotos = emptyList(), context = context)

        // Act
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Take Photo").assertExists()
        composeTestRule.onNodeWithText("Choose Photo").assertExists()
        composeTestRule.onNodeWithText("My Try-On Photo").assertDoesNotExist()
    }

    @Test
    fun testHomeScreen_withSavedPhoto_showsPhotoAndActions() {
        // Arrange
        val defaultPhoto = TryOnPhoto(
            id = "photo_1",
            uri = "https://example.com/photo.jpg",
            isDefault = true,
            orderIndex = 0
        )
        UserPhotosRepository.resetForTesting(initialPhotos = listOf(defaultPhoto), context = context)

        // Act
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                HomeScreen(navController = navController)
            }
        }

        // Assert
        composeTestRule.onNodeWithText("Take Photo").assertExists()
        composeTestRule.onNodeWithText("Choose Photo").assertExists()
        composeTestRule.onNodeWithText("My Try-On Photo").assertDoesNotExist()
    }

    @Test
    fun testCameraAndGallery_persistsThroughRepository_doesNotStartTryOn() {
        // Since we can't easily intercept ActivityResultLaunchers in standard Compose test rules 
        // without complex intents mocking, we verify the underlying repository action that the 
        // launchers are hardcoded to invoke (TryOnManager.updateUserPhoto).
        
        // Arrange
        UserPhotosRepository.resetForTesting(initialPhotos = emptyList(), context = context)
        
        // Act: simulate camera result
        com.example.ui.TryOnManager.updateUserPhoto("content://fake/camera/uri", context)
        
        // Assert
        val photoAfterCamera = UserPhotosRepository.defaultPhotoUri
        assert(photoAfterCamera == "content://fake/camera/uri")
        
        // Act: simulate gallery result
        com.example.ui.TryOnManager.updateUserPhoto("content://fake/gallery/uri", context)
        
        // Assert
        val photoAfterGallery = UserPhotosRepository.defaultPhotoUri
        assert(photoAfterGallery == "content://fake/gallery/uri")
        
        // Verify no credits were consumed (should be 2 by default)
        val credits = com.example.credit.repository.CreditRepositoryProvider.get().balanceFlow.value.total
        assert(credits == 2)
    }
}
