package com.example

import com.example.ui.SessionManager
import com.example.credit.repository.CreditRepositoryProvider
import com.example.credit.repository.FakeCreditRepository
import com.example.ui.TryOnManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PhotoFlowUnitTest {

    @Before
    fun setUp() {
        com.example.credit.repository.CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        SessionManager.isGuest = false
        SessionManager.resetForTesting(initialFree = 10, initialPurchased = 0)
        SessionManager.releaseHeldCredit()
        TryOnManager.selectedUserPhotoUri = ""
        TryOnManager.selectedProductId = "prod_default"
        TryOnManager.selectedProductName = "Default Outfit"
    }

    @Test
    fun testInitialUserPhotoState_isEmpty() {
        // BUG-12: Verify no hardcoded default user photo exists
        assertEquals("", TryOnManager.selectedUserPhotoUri)
    }

    @Test
    fun testUpdateUserPhoto_successUpdatesUri() {
        val capturedUri = "content://com.aistudio.onme.tryonx.fileprovider/camera_photos/test_photo.jpg"
        TryOnManager.updateUserPhoto(capturedUri)

        assertEquals(capturedUri, TryOnManager.selectedUserPhotoUri)
    }

    @Test
    fun testUpdateUserPhoto_nullOrBlankDoesNotReplaceExistingPhoto() {
        val existingUri = "content://media/external/images/media/12345"
        TryOnManager.updateUserPhoto(existingUri)
        assertEquals(existingUri, TryOnManager.selectedUserPhotoUri)

        // Cancelled or null result
        TryOnManager.updateUserPhoto(null)
        assertEquals(existingUri, TryOnManager.selectedUserPhotoUri)

        // Blank result
        TryOnManager.updateUserPhoto("")
        assertEquals(existingUri, TryOnManager.selectedUserPhotoUri)
        TryOnManager.updateUserPhoto("   ")
        assertEquals(existingUri, TryOnManager.selectedUserPhotoUri)
    }

    @Test
    fun testPhotoAction_doesNotModifyProductMetadata() {
        val originalProductId = TryOnManager.selectedProductId
        val originalProductName = TryOnManager.selectedProductName

        // User acquires photo (Flow A)
        val photoUri = "content://media/picker/987"
        TryOnManager.updateUserPhoto(photoUri)

        // Product selection must remain untouched
        assertEquals(originalProductId, TryOnManager.selectedProductId)
        assertEquals(originalProductName, TryOnManager.selectedProductName)
        assertNotEquals("prod_camera", TryOnManager.selectedProductId)
        assertNotEquals("prod_featured", TryOnManager.selectedProductId)
    }

    @Test
    fun testPhotoAction_doesNotAffectCredits() {
        val initialCredits = SessionManager.credits
        val photoUri = "content://media/picker/987"

        // Acquiring photo
        TryOnManager.updateUserPhoto(photoUri)

        // Credits and hold state must remain unaffected
        assertEquals(initialCredits, SessionManager.credits)
        assertFalse(SessionManager.hasActiveHold)
        assertEquals(initialCredits, SessionManager.availableCredits)
    }

    @Test
    fun testEmptyPhotoState_preventsTryOnConfirmation() {
        // With empty photo, valid photo check fails
        TryOnManager.selectedUserPhotoUri = ""
        val hasValidPhoto = TryOnManager.selectedUserPhotoUri.isNotBlank()
        assertFalse(hasValidPhoto)

        // Once photo is added, check passes
        TryOnManager.updateUserPhoto("content://media/external/images/media/100")
        val hasValidPhotoNow = TryOnManager.selectedUserPhotoUri.isNotBlank()
        assertTrue(hasValidPhotoNow)
    }
}
