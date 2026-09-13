package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.ui.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.example.credit.repository.CreditRepositoryProvider
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class PersistenceIntegrationTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        com.example.credit.repository.CreditRepositoryProvider.init(androidx.test.core.app.ApplicationProvider.getApplicationContext())
        // Initialize clean persistence state
        SessionManager.resetForTesting(
            initialGuest = false,
            initialFree = 2,
            initialPurchased = 0,
            initialNotifications = true,
            context = context
        )
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
        UserPhotosRepository.resetForTesting(context = context)
        TiHinStyleRepository.resetForTesting(context = context)
    }

    @After
    fun tearDown() {
        // Clean up
    }

    /**
     * 1. Edit profile displayName and photo -> close app -> restart app -> values remain.
     */
    @Test
    fun test1_profilePersistenceAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        // Edit profile
        UserProfileRepository.updateProfile(
            displayName = "Maya S. Kapoor",
            profilePhotoUri = "https://example.com/avatar.jpg",
            context = context
        )
        assertEquals("Maya S. Kapoor", UserProfileRepository.profile.displayName)
        assertEquals("https://example.com/avatar.jpg", UserProfileRepository.profile.profilePhotoUri)

        // Simulate app restart
        UserProfileRepository.init(context)

        assertEquals("Maya S. Kapoor", UserProfileRepository.profile.displayName)
        assertEquals("https://example.com/avatar.jpg", UserProfileRepository.profile.profilePhotoUri)
    
}
}

    /**
     * 2. Add new Try-On photo in My Try-On Photos -> set as default -> restart app -> still default photo.
     */
    @Test
    fun test2_userPhotosPersistenceAndDefaultSelection() {
kotlinx.coroutines.runBlocking {

        UserPhotosRepository.initSync(context)
        val initialCount = UserPhotosRepository.photos.size

        // Add a new photo
        val newPhotoUri = "https://example.com/new_tryon_photo.jpg"
        val added = UserPhotosRepository.addPhotoSync(context, newPhotoUri, setAsDefault = true)
        assertTrue(added)
        assertEquals(initialCount + 1, UserPhotosRepository.photos.size)

        val newlyAdded = UserPhotosRepository.photos.find { it.uri == newPhotoUri }
        assertNotNull(newlyAdded)
        assertTrue(newlyAdded!!.isDefault)
        assertEquals(newPhotoUri, UserPhotosRepository.defaultPhotoUri)

        // Simulate app restart
        UserPhotosRepository.initSync(context)

        val restoredPhoto = UserPhotosRepository.photos.find { it.uri == newPhotoUri }
        assertNotNull(restoredPhoto)
        assertTrue(restoredPhoto!!.isDefault)
        assertEquals(newPhotoUri, UserPhotosRepository.defaultPhotoUri)
    
}
}

    /**
     * 3. Favourite a product in Discover -> restart app -> still favourited in Discover and in Looks -> Favourites.
     */
    @Test
    fun test3_favouritePersistenceAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        TiHinStyleRepository.initSync(context)
        assertFalse(TiHinStyleRepository.isFavourite("p_dress_101"))

        // Favourite product
        TiHinStyleRepository.toggleFavourite(
            productId = "p_dress_101",
            productName = "Silk Evening Gown",
            merchant = "Zara",
            price = 7999.0,
            imageUrl = "https://example.com/dress.jpg",
            context = context,
            synchronous = true
        )
        assertTrue(TiHinStyleRepository.isFavourite("p_dress_101"))
        assertTrue(TiHinStyleRepository.isPriceTracked("p_dress_101"))

        // Simulate app restart
        TiHinStyleRepository.initSync(context)

        assertTrue(TiHinStyleRepository.isFavourite("p_dress_101"))
        assertTrue(TiHinStyleRepository.isPriceTracked("p_dress_101"))
    
}
}

    /**
     * 4. Unfavourite a product in Looks -> restart app -> product remains unfavourited.
     */
    @Test
    fun test4_unfavouritePersistenceAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        TiHinStyleRepository.initSync(context)

        // Favourite first
        TiHinStyleRepository.setFavourite(productId = "p_shoe_202", isFav = true, context = context, synchronous = true)
        assertTrue(TiHinStyleRepository.isFavourite("p_shoe_202"))

        // Unfavourite
        TiHinStyleRepository.setFavourite(productId = "p_shoe_202", isFav = false, context = context, synchronous = true)
        assertFalse(TiHinStyleRepository.isFavourite("p_shoe_202"))

        // Simulate app restart
        TiHinStyleRepository.initSync(context)

        assertFalse(TiHinStyleRepository.isFavourite("p_shoe_202"))
    
}
}

    /**
     * 5. Track a product and set target price (e.g. ₹4,499) -> restart app -> product remains tracked with target price ₹4,499.
     */
    @Test
    fun test5_priceTrackingAndTargetPriceAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        TiHinStyleRepository.initSync(context)

        // Track a product
        TiHinStyleRepository.setPriceTracking(
            productId = "p_jacket_303",
            enabled = true,
            productName = "Leather Biker Jacket",
            merchant = "Mango",
            price = 4999.0,
            imageUrl = "https://example.com/jacket.jpg",
            context = context,
            synchronous = true
        )
        assertTrue(TiHinStyleRepository.isPriceTracked("p_jacket_303"))

        // Set target price to 4499.0
        TiHinStyleRepository.setTargetPrice(
            productId = "p_jacket_303",
            targetPrice = 4499.0,
            context = context,
            synchronous = true
        )
        assertEquals(4499.0, TiHinStyleRepository.getTargetPrice("p_jacket_303")!!, 0.01)

        // Simulate app restart
        TiHinStyleRepository.initSync(context)

        assertTrue(TiHinStyleRepository.isPriceTracked("p_jacket_303"))
        assertEquals(4499.0, TiHinStyleRepository.getTargetPrice("p_jacket_303")!!, 0.01)
    
}
}

    /**
     * 6. Toggle Price Drop Notifications setting -> restart app -> setting remains preserved.
     */
    @Test
    fun test6_priceDropNotificationsSettingPreservedAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        SessionManager.init(context)
        assertTrue(SessionManager.priceDropNotificationsEnabled)

        // Turn notifications off
        SessionManager.setPriceDropNotifications(false, context)
        assertFalse(SessionManager.priceDropNotificationsEnabled)

        // Simulate app restart
        SessionManager.init(context)

        assertFalse(SessionManager.priceDropNotificationsEnabled)
    
}
}

    /**
     * 7. Generate a Try-On look -> save look -> restart app -> saved look appears at top of Looks -> Recent.
     */
    @Test
    fun test7_savedTryOnResultPersistenceAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        TiHinStyleRepository.initSync(context)

        val result = TryOnResult(
            id = "res_saved_999",
            userPhoto = "https://example.com/user_photo.jpg",
            outfitImage = "https://example.com/outfit.jpg",
            resultImage = "https://example.com/result_999.jpg",
            createdAt = "Just now",
            isFavourite = true,
            productId = "p_jacket_303",
            productName = "Leather Biker Jacket",
            productBrand = "Mango",
            productPrice = 4999.0,
            cardHeight = 280
        )

        TiHinStyleRepository.saveResult(result, context, synchronous = true)
        assertEquals(1, TiHinStyleRepository.savedResults.size)
        assertEquals("res_saved_999", TiHinStyleRepository.savedResults.first().id)

        // Simulate app restart
        TiHinStyleRepository.initSync(context)

        assertEquals(1, TiHinStyleRepository.savedResults.size)
        assertEquals("res_saved_999", TiHinStyleRepository.savedResults.first().id)
        assertEquals("https://example.com/result_999.jpg", TiHinStyleRepository.savedResults.first().resultImage)
    
}
}

    /**
     * 8. Authenticated user receives 2 free credits -> use 1 credit -> restart app -> 1 credit remains.
     */
    @Test
    fun test8_creditConsumptionPersistenceAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        SessionManager.init(context)
        kotlinx.coroutines.delay(100)
        val repo = CreditRepositoryProvider.get()
        assertEquals(2, repo.balanceFlow.value.freeCredits)
        assertEquals(0, repo.balanceFlow.value.purchasedCredits)
        assertEquals(2, repo.balanceFlow.value.total)

        // Hold and consume 1 credit
        assertTrue(repo.hold("test-op"))
        assertTrue(repo.consume("test-op"))
        
        assertEquals(1, repo.balanceFlow.value.freeCredits)
        assertEquals(0, repo.balanceFlow.value.purchasedCredits)
        assertEquals(1, repo.balanceFlow.value.total)

        // Simulate app restart
        SessionManager.init(context)
        kotlinx.coroutines.delay(100)
        assertEquals(1, repo.balanceFlow.value.freeCredits)
        assertEquals(0, repo.balanceFlow.value.purchasedCredits)
        assertEquals(1, repo.balanceFlow.value.total)
    
}
}

    /**
     * 9. Buy 5 credits -> total updates -> restart app -> purchased credits remain intact.
     */
    @Test
    fun test9_purchasedCreditsPersistenceAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        SessionManager.init(context)
        assertEquals(2, SessionManager.freeCredits)
        assertEquals(0, SessionManager.purchasedCredits)

        // Purchase 5 credits
        SessionManager.addPurchasedCredits(5, context)
        kotlinx.coroutines.delay(100)
        assertEquals(2, SessionManager.freeCredits)
        assertEquals(5, SessionManager.purchasedCredits)
        assertEquals(7, SessionManager.credits)

        // Simulate app restart
        SessionManager.init(context)

        assertEquals(2, SessionManager.freeCredits)
        assertEquals(5, SessionManager.purchasedCredits)
        kotlinx.coroutines.delay(100)
        assertEquals(7, SessionManager.credits)
    
}
}

    /**
     * 10. Guest user state: 0 credits, restricted saved state -> restart app -> guest state preserved.
     */
    @Test
    fun test10_guestUserStatePreservedAcrossRestarts() {
kotlinx.coroutines.runBlocking {

        SessionManager.init(context)
        SessionManager.setSessionMode(asGuest = true, context = context)

        assertTrue(SessionManager.isGuest)
        assertEquals(0, SessionManager.freeCredits)
        assertEquals(0, SessionManager.purchasedCredits)
        assertEquals(0, SessionManager.credits)

        // Simulate app restart
        SessionManager.init(context)

        assertTrue(SessionManager.isGuest)
        assertEquals(0, SessionManager.credits)
    
}
}

    /**
     * 11. Log out -> restart app -> logged-out / guest state preserved without crash or data corruption.
     */
    @Test
    fun test11_logoutPreservesLoggedOutStateWithoutCorruptingUserData() {
kotlinx.coroutines.runBlocking {

        SessionManager.init(context)
        assertEquals(false, SessionManager.isGuest)

        // Perform logout
        SessionManager.logout(context)

        assertTrue(SessionManager.isGuest)
        assertEquals(0, SessionManager.credits)

        // Simulate app restart
        SessionManager.init(context)

        assertTrue(SessionManager.isGuest)
        assertEquals(0, SessionManager.credits)
    
}
}
}
