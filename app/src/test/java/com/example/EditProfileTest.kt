package com.example



import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.ui.EditProfileScreen
import com.example.ui.MeScreen
import com.example.ui.SessionManager
import com.example.credit.repository.CreditRepositoryProvider
import com.example.ui.UserProfile
import com.example.ui.UserProfileRepository
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
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
class EditProfileTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        
        SessionManager.isGuest = false
        UserProfileRepository.resetForTesting(
            customProfile = UserProfile(
                userId = "usr_maya_01",
                displayName = "Maya Sharma",
                email = "maya.sharma@example.com",
                profilePhotoUri = null,
                authProvider = "Google"
            ),
            context = context
        )
    }

    @Test
    fun testMeScreen_authenticatedUserSeesEditProfileButton() {
kotlinx.coroutines.runBlocking {


        SessionManager.isGuest = false

        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                MeScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithTag("me_profile_name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Maya Sharma").assertIsDisplayed()
        composeTestRule.onNodeWithTag("me_profile_email").assertIsDisplayed()
        composeTestRule.onNodeWithText("maya.sharma@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithTag("edit_profile_button").assertIsDisplayed()
    

}
}

    @Test
    fun testMeScreen_guestUserDoesNotSeeEditProfileButton() {
kotlinx.coroutines.runBlocking {


        SessionManager.isGuest = true

        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                MeScreen(navController = navController)
            }
        }

        // Guest avatar and create account shown
        composeTestRule.onNodeWithText("Guest").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Create Account").onFirst().assertIsDisplayed()
        // Edit Profile button must NOT be present for guests
        composeTestRule.onNodeWithTag("edit_profile_button").assertDoesNotExist()
    

}
}

    @Test
    fun testEditProfile_prefillsExistingNameAndDisplaysEmail() {
kotlinx.coroutines.runBlocking {


        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                EditProfileScreen(navController = navController)
            }
        }

        // Screen title and back button
        composeTestRule.onNodeWithText("Edit Profile").assertIsDisplayed()
        composeTestRule.onNodeWithTag("edit_profile_back_button").assertIsDisplayed()

        // Name field pre-filled
        composeTestRule.onNodeWithTag("name_input_field").assertIsDisplayed()
        composeTestRule.onNodeWithText("Maya Sharma").assertIsDisplayed()

        // Email field displayed
        composeTestRule.onNodeWithTag("email_input_field").assertIsDisplayed()
        composeTestRule.onNodeWithText("maya.sharma@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email is linked to your account and cannot be edited.").assertIsDisplayed()

        // Save Changes CTA
        composeTestRule.onNodeWithTag("save_changes_button").performScrollTo().assertIsDisplayed()
    

}
}

    @Test
    fun testEditProfile_nameCanBeEditedAndSaved() {
kotlinx.coroutines.runBlocking {


        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                EditProfileScreen(navController = navController)
            }
        }

        // Edit Name
        composeTestRule.onNodeWithTag("name_input_field").performTextClearance()
        composeTestRule.onNodeWithTag("name_input_field").performTextInput("Aria Patel")
        composeTestRule.waitForIdle()

        // Click Save Changes
        composeTestRule.onNodeWithTag("save_changes_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Verify repository state was updated
        assertEquals("Aria Patel", UserProfileRepository.profile.displayName)
    

}
}

    @Test
    fun testEditProfile_emptyNameIsRejected() {
kotlinx.coroutines.runBlocking {


        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                EditProfileScreen(navController = navController)
            }
        }

        // Clear name to empty
        composeTestRule.onNodeWithTag("name_input_field").performTextClearance()
        composeTestRule.onNodeWithTag("name_input_field").performTextInput("   ")
        composeTestRule.waitForIdle()

        // Attempt to save
        composeTestRule.onNodeWithTag("save_changes_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Error displayed and repository name remains unchanged
        composeTestRule.onNodeWithTag("name_input_field").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Name cannot be empty").assertExists()
        assertEquals("Maya Sharma", UserProfileRepository.profile.displayName)
    

}
}

    @Test
    fun testEditProfile_meScreenShowsUpdatedNameAndPhoto() {
kotlinx.coroutines.runBlocking {


        // Direct profile update
        UserProfileRepository.updateProfile(
            displayName = "Ananya Sen",
            profilePhotoUri = "content://media/external/images/media/456",
            context = context
        )

        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                MeScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithTag("me_profile_name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ananya Sen").assertIsDisplayed()
        composeTestRule.onNodeWithText("maya.sharma@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithTag("me_profile_photo").assertIsDisplayed()
    

}
}

    @Test
    fun testEditProfile_photoPickerFlowPresentsOptions() {
kotlinx.coroutines.runBlocking {


        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                EditProfileScreen(navController = navController)
            }
        }

        // Tap Change Photo
        composeTestRule.onNodeWithTag("change_photo_button").performClick()
        composeTestRule.waitForIdle()

        // Verify bottom sheet appears with Camera and Gallery choices
        composeTestRule.onNodeWithTag("choose_camera_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("choose_gallery_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Choose from Gallery").assertIsDisplayed()
    

}
}

    @Test
    fun testEditProfile_unsavedChangesTriggersDiscardDialog() {
kotlinx.coroutines.runBlocking {


        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                EditProfileScreen(navController = navController)
            }
        }

        // Modify name
        composeTestRule.onNodeWithTag("name_input_field").performTextInput(" Updated")
        composeTestRule.waitForIdle()

        // Tap back button
        composeTestRule.onNodeWithTag("edit_profile_back_button").performClick()
        composeTestRule.waitForIdle()

        // Discard dialog is displayed
        composeTestRule.onNodeWithText("Discard changes?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("discard_dialog_cancel_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("discard_dialog_confirm_button").assertIsDisplayed()

        // Tap Keep Editing
        composeTestRule.onNodeWithTag("discard_dialog_cancel_button").performClick()
        composeTestRule.waitForIdle()

        // Dialog dismissed and screen still active
        composeTestRule.onNodeWithText("Discard changes?").assertDoesNotExist()
        composeTestRule.onNodeWithText("Edit Profile").assertIsDisplayed()
    

}
}

    @Test
    fun testEditProfile_noUnsavedChangesDoesNotShowDiscardDialog() {
kotlinx.coroutines.runBlocking {


        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                EditProfileScreen(navController = navController)
            }
        }

        // Tap back without making any changes
        composeTestRule.onNodeWithTag("edit_profile_back_button").performClick()
        composeTestRule.waitForIdle()

        // Discard dialog should NOT be displayed
        composeTestRule.onNodeWithText("Discard changes?").assertDoesNotExist()
    

}
}

    @Test
    fun testEditProfile_guestUserDoesNotReceiveEditableProfile() {
kotlinx.coroutines.runBlocking {


        SessionManager.isGuest = true

        Thread.sleep(500)
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                EditProfileScreen(navController = navController)
            }
        }

        // Guest fallback UI displayed
        composeTestRule.onNodeWithText("Guest").assertIsDisplayed()
        composeTestRule.onNodeWithTag("guest_create_account_button").assertIsDisplayed()

        // No editable profile inputs or save button
        composeTestRule.onNodeWithTag("name_input_field").assertDoesNotExist()
        composeTestRule.onNodeWithTag("email_input_field").assertDoesNotExist()
        composeTestRule.onNodeWithTag("save_changes_button").assertDoesNotExist()
    

}
}

    @Test
    fun testUserProfileRepository_persistenceSurvivesInit() {
        // Update profile with persistence
        UserProfileRepository.updateProfile(
            displayName = "Maya Kapoor",
            profilePhotoUri = "file:///data/user/0/com.example/camera_photos/profile_test.jpg",
            context = context
        )

        assertEquals("Maya Kapoor", UserProfileRepository.profile.displayName)
        assertEquals("file:///data/user/0/com.example/camera_photos/profile_test.jpg", UserProfileRepository.profile.profilePhotoUri)

        // Reset in-memory state to simulate fresh app restart
        UserProfileRepository.resetForTesting(
            customProfile = UserProfile(displayName = "Fallback Name"),
            context = null
        )
        assertEquals("Fallback Name", UserProfileRepository.profile.displayName)

        // Re-initialize from persistent SharedPreferences
        UserProfileRepository.init(context)

        assertEquals("Maya Kapoor", UserProfileRepository.profile.displayName)
        assertEquals("file:///data/user/0/com.example/camera_photos/profile_test.jpg", UserProfileRepository.profile.profilePhotoUri)
        assertEquals("maya.sharma@example.com", UserProfileRepository.profile.email)
    }
}
