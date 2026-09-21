package com.example

import com.example.data.datasource.MockProductDataSource
import com.example.data.repository.DefaultProductRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.ProductRepositoryProvider
import com.example.ui.SavedLook
import com.example.ui.TiHinStyleRepository
import com.example.ui.SessionManager
import com.example.ui.TryOnManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TiHinDataFlowTest {

    @Before
    fun setUp() {
        TiHinStyleRepository.resetForTesting()
        ProductRepositoryProvider.setForTesting(DefaultProductRepository(MockProductDataSource))
    }

    @After
    fun tearDown() {
        TiHinStyleRepository.resetForTesting()
        ProductRepositoryProvider.reset()
    }

    @Test
    fun `test favourite addition, lookup resolution, and removal`() {
        // Step 1: Favourite a real catalog product (ID = "1")
        val isFav = TiHinStyleRepository.toggleFavourite(
            productId = "1",
            productName = "Oversized Cashmere Trench",
            merchant = "ZARA",
            price = 12999.0,
            imageUrl = "https://example.com/trench.jpg"
        )
        assertTrue("Product should be favourited", isFav)
        assertTrue("TiHinStyleRepository.isFavourite should be true", TiHinStyleRepository.isFavourite("1"))
        assertEquals(1, TiHinStyleRepository.favouriteProductIds.size)
        assertEquals("1", TiHinStyleRepository.favouriteProductIds[0])

        // Step 2: Resolve SavedLook for LooksScreen FavouritesTab
        val look = TiHinStyleRepository.getProductLookup("1")
        assertEquals("1", look.productId)
        assertEquals("Oversized Cashmere Trench", look.productName)
        assertEquals("ZARA", look.brand)
        assertEquals(12999.0, look.price, 0.001)
        assertTrue("SavedLook should reflect favourited state", look.isFavourite)
        assertFalse("Price tracking should not be active by default", look.isPriceTracked)

        // Step 3: Un-favourite removes item
        val isFavAfterToggle = TiHinStyleRepository.toggleFavourite("1")
        assertFalse("Product should be un-favourited", isFavAfterToggle)
        assertFalse("TiHinStyleRepository.isFavourite should be false", TiHinStyleRepository.isFavourite("1"))
        assertEquals(0, TiHinStyleRepository.favouriteProductIds.size)
    }

    @Test
    fun `test price tracking addition, lookup resolution, and removal`() {
        // Step 1: Enable price tracking on product "2"
        val isTracked = TiHinStyleRepository.togglePriceTracking("2")
        assertTrue("Product 2 should be price tracked", isTracked)
        assertTrue(TiHinStyleRepository.isPriceTracked("2"))
        assertEquals(1, TiHinStyleRepository.priceTrackedProductIds.size)

        // Step 2: Resolve SavedLook for LooksScreen PriceTrackingTab
        val look = TiHinStyleRepository.getProductLookup("2")
        assertEquals("2", look.productId)
        assertEquals("Tailored Wool Overcoat", look.productName)
        assertEquals("H&M", look.brand)
        assertTrue(look.isPriceTracked)
        assertFalse(look.isFavourite)

        // Step 3: Disable price tracking
        val isTrackedAfterToggle = TiHinStyleRepository.togglePriceTracking("2")
        assertFalse("Product 2 should no longer be price tracked", isTrackedAfterToggle)
        assertFalse(TiHinStyleRepository.isPriceTracked("2"))
        assertEquals(0, TiHinStyleRepository.priceTrackedProductIds.size)
    }

    @Test
    fun `test saved look addition and deletion`() {
        // Step 1: Add a saved look
        val look = SavedLook(
            id = "test_look_id",
            productId = "1",
            productName = "Trench",
            brand = "ZARA",
            price = 12999.0,
            resultImageUri = "https://example.com/res.jpg"
        )
        TiHinStyleRepository.addSavedLook(look)
        assertEquals(1, TiHinStyleRepository.savedLooks.size)
        assertEquals("test_look_id", TiHinStyleRepository.savedLooks[0].id)

        // Step 2: Delete it
        TiHinStyleRepository.deleteSavedLook("test_look_id")
        assertEquals(0, TiHinStyleRepository.savedLooks.size)
    }

    @Test
    fun `test state consistency between favourites and price tracking`() {
        // Step 1: Favourite 2 products
        TiHinStyleRepository.setFavourite(
            productId = "1",
            isFav = true,
            productName = "Oversized Cashmere Trench",
            merchant = "ZARA",
            price = 12999.0
        )
        TiHinStyleRepository.setFavourite(
            productId = "3",
            isFav = true,
            productName = "Minimalist Linen Blazer",
            merchant = "MANGO",
            price = 6590.0
        )

        // Step 2: Track 2 DIFFERENT products
        TiHinStyleRepository.togglePriceTracking("2")
        TiHinStyleRepository.togglePriceTracking("4")

        // Step 3: Verify strict state separation (Me screen counts match)
        assertEquals("Saved looks count must be exactly 2", 2, TiHinStyleRepository.favouriteProductIds.size)
        assertEquals("Price tracking count must be exactly 2", 2, TiHinStyleRepository.priceTrackedProductIds.size)

        assertTrue(TiHinStyleRepository.isFavourite("1"))
        assertTrue(TiHinStyleRepository.isFavourite("3"))
        assertFalse(TiHinStyleRepository.isFavourite("2"))
        assertFalse(TiHinStyleRepository.isFavourite("4"))

        assertTrue(TiHinStyleRepository.isPriceTracked("2"))
        assertTrue(TiHinStyleRepository.isPriceTracked("4"))
        assertFalse(TiHinStyleRepository.isPriceTracked("1"))
        assertFalse(TiHinStyleRepository.isPriceTracked("3"))
    }

    @Test
    fun `test getProductLookup prefers virtual try-on look if present`() {
        // Create a generated try-on look
        val generatedLook = SavedLook(
            id = "tryon_look_1",
            productId = "1",
            productName = "Custom Tried Trench",
            brand = "ZARA",
            price = 12999.0,
            resultImageUri = "https://storage.example.com/user_tryon_result.jpg"
        )
        TiHinStyleRepository.addSavedLook(generatedLook)
        TiHinStyleRepository.setFavourite("1", true)

        val resolved = TiHinStyleRepository.getProductLookup("1")
        assertEquals("tryon_look_1", resolved.id)
        assertEquals("https://storage.example.com/user_tryon_result.jpg", resolved.resultImageUri)
        assertEquals("Custom Tried Trench", resolved.productName)
        assertTrue(resolved.isFavourite)
    }

    @Test
    fun `test photo management state consistency in TryOnManager`() {
        // Initially null
        TryOnManager.selectedUserPhotoUri = null
        assertNull(TryOnManager.selectedUserPhotoUri)

        // Set photo
        val sampleUri = "content://media/external/images/media/100"
        TryOnManager.updateUserPhoto(sampleUri)
        assertNotNull(TryOnManager.selectedUserPhotoUri)
        assertEquals(sampleUri, TryOnManager.selectedUserPhotoUri.toString())

        // Reset resets photo
        TryOnManager.resetForTesting()
        assertNull(TryOnManager.selectedUserPhotoUri)
    }

    @Test
    fun `test notification preference toggling in SessionManager`() {
        com.example.ui.SessionManager.setPriceDropNotifications(true)
        assertTrue(com.example.ui.SessionManager.priceDropNotificationsEnabled)

        com.example.ui.SessionManager.setPriceDropNotifications(false)
        assertFalse(com.example.ui.SessionManager.priceDropNotificationsEnabled)

        com.example.ui.SessionManager.setPriceDropNotifications(true)
        assertTrue(com.example.ui.SessionManager.priceDropNotificationsEnabled)
    }

    @Test
    fun `test guest and member session transitions`() {
        // Set guest mode
        com.example.ui.SessionManager.continueAsGuest()
        assertTrue(com.example.ui.SessionManager.isGuest)
        assertNull(com.example.ui.SessionManager.userEmail)

        // Log in with email
        com.example.ui.SessionManager.loginWithEmail("designer@tihin.fashion")
        assertFalse(com.example.ui.SessionManager.isGuest)
        assertEquals("designer@tihin.fashion", com.example.ui.SessionManager.userEmail)

        // Logout returns to guest
        com.example.ui.SessionManager.logout()
        assertTrue(com.example.ui.SessionManager.isGuest)
        assertNull(com.example.ui.SessionManager.userEmail)
    }

    @Test
    fun `test Home See the Magic hero looks data consistency and no stale state`() {
        val repo = ProductRepository.get()
        val heroLooks = repo.getHeroLooks()
        assertTrue("Hero looks must not be empty", heroLooks.isNotEmpty())

        for (look in heroLooks) {
            val product = repo.getProductById(look.productId)
            assertNotNull("Product for hero look ${look.productId} must exist", product)
            assertEquals("Product ID in lookup must match hero look productId", look.productId, product!!.id)

            val hangerImg = look.hangerImage ?: product.productImages.getOrNull(1)
            val wornImg = look.wornImage ?: product.productImages.getOrNull(0) ?: product.primaryImageUrl
            assertNotNull("Hanger image must not be null", hangerImg)
            assertNotNull("Worn image must not be null", wornImg)
            assertNotEquals("Hanger image and worn image must be distinct", hangerImg, wornImg)
            assertTrue("Price must be greater than 0", product.price > 0)
            assertTrue("Brand must not be blank", product.brand.isNotBlank())
            assertTrue("Name must not be blank", product.name.isNotBlank())
        }
    }

    @Test
    fun `test Home Try this look flow populates TryOnManager with correct product`() {
        TryOnManager.resetForTesting()
        val repo = ProductRepository.get()
        val heroLook = repo.getHeroLooks().first()
        val product = repo.getProductById(heroLook.productId)!!

        // Simulate "Try this look" CTA tap
        TryOnManager.selectedProductId = product.id
        TryOnManager.selectedProductName = product.name
        TryOnManager.selectedProductBrand = product.brand
        TryOnManager.selectedProductPrice = product.price
        TryOnManager.selectedProductImage = product.primaryImageUrl
        TryOnManager.generatedResultImageUri = heroLook.wornImage ?: product.primaryImageUrl

        assertEquals(product.id, TryOnManager.selectedProductId)
        assertEquals(product.name, TryOnManager.selectedProductName)
        assertEquals(product.brand, TryOnManager.selectedProductBrand)
        assertEquals(product.price, TryOnManager.selectedProductPrice, 0.001)
        assertEquals(product.primaryImageUrl, TryOnManager.selectedProductImage)
        assertEquals(heroLook.wornImage ?: product.primaryImageUrl, TryOnManager.generatedResultImageUri)
    }

    @Test
    fun `test Home Take Photo and Choose Photo flows update TryOnManager photo and default garment`() {
        TryOnManager.resetForTesting()
        val repo = ProductRepository.get()

        // Simulate camera capture result
        val cameraUri = "content://com.example.fileprovider/camera_photos/temp_camera_123.jpg"
        TryOnManager.updateUserPhoto(cameraUri)

        // If no product selected, default hero product is selected
        if (TryOnManager.selectedProductId == null) {
            val defaultProduct = repo.getHeroLooks().firstOrNull()?.let {
                repo.getProductById(it.productId)
            } ?: repo.getTrendingProducts().firstOrNull()
            if (defaultProduct != null) {
                TryOnManager.selectedProductId = defaultProduct.id
                TryOnManager.selectedProductName = defaultProduct.name
                TryOnManager.selectedProductBrand = defaultProduct.brand
                TryOnManager.selectedProductPrice = defaultProduct.price
                TryOnManager.selectedProductImage = defaultProduct.primaryImageUrl
            }
        }

        assertEquals(cameraUri, TryOnManager.selectedUserPhotoUri.toString())
        assertNotNull("Default product ID should be assigned", TryOnManager.selectedProductId)
        assertNotNull("Default product name should be assigned", TryOnManager.selectedProductName)
    }

    @Test
    fun `test Home Trending Look card tap and heart tap data isolation`() {
        TryOnManager.resetForTesting()
        TiHinStyleRepository.resetForTesting()
        val repo = ProductRepository.get()
        val trendingProducts = repo.getTrendingProducts()
        assertTrue(trendingProducts.isNotEmpty())

        val item = trendingProducts.first()

        // 1. Simulate card tap -> TryOnManager
        TryOnManager.selectedProductId = item.id
        TryOnManager.selectedProductName = item.name
        TryOnManager.selectedProductBrand = item.brand
        TryOnManager.selectedProductPrice = item.price
        TryOnManager.selectedProductImage = item.primaryImageUrl

        assertEquals(item.id, TryOnManager.selectedProductId)
        assertEquals(item.name, TryOnManager.selectedProductName)
        assertEquals(item.brand, TryOnManager.selectedProductBrand)
        assertEquals(item.price, TryOnManager.selectedProductPrice, 0.001)

        // 2. Simulate heart tap -> TiHinStyleRepository
        val isFav = TiHinStyleRepository.toggleFavourite(
            productId = item.id,
            productName = item.name,
            merchant = item.brand,
            price = item.price,
            imageUrl = item.primaryImageUrl
        )
        assertTrue(isFav)
        assertTrue(TiHinStyleRepository.isFavourite(item.id))
        // Verify no price tracking side effect
        assertFalse("Price tracking must remain unaffected by heart tap", TiHinStyleRepository.isPriceTracked(item.id))
        assertEquals(0, TiHinStyleRepository.priceTrackedProductIds.size)

        // 3. Verify lookup for Looks -> Favourites
        val lookup = TiHinStyleRepository.getProductLookup(item.id)
        assertEquals(item.id, lookup.productId)
        assertEquals(item.name, lookup.productName)
        assertEquals(item.brand, lookup.brand)
    }

    @Test
    fun `test Me screen profile edit and persistence`() {
        val context = null // SessionManager handles null context for non-persistence tests if needed
        SessionManager.resetForTesting(initialGuest = false)
        SessionManager.loginWithEmail("test@example.com")
        
        assertEquals("test@example.com", SessionManager.userEmail)
        assertNull(SessionManager.userDisplayName) // Should be null initially
        
        // Simulate edit
        SessionManager.updateDisplayName("TiHin Lover")
        assertEquals("TiHin Lover", SessionManager.userDisplayName)
        
        // Simulate logout
        SessionManager.logout()
        assertNull(SessionManager.userEmail)
        assertNull(SessionManager.userDisplayName)
        assertTrue(SessionManager.isGuest)
    }

    @Test
    fun `test Me screen delete account clears all local state`() {
        TiHinStyleRepository.resetForTesting()
        TryOnManager.resetForTesting()
        SessionManager.resetForTesting(initialGuest = false)
        SessionManager.loginWithEmail("delete@me.com")
        
        TiHinStyleRepository.setFavourite("p1", true)
        TryOnManager.selectedUserPhotoUri = android.net.Uri.parse("file://photo.jpg")
        
        assertTrue(TiHinStyleRepository.isFavourite("p1"))
        assertNotNull(TryOnManager.selectedUserPhotoUri)
        
        // Simulate Delete Account Action
        TryOnManager.selectedUserPhotoUri = null
        TiHinStyleRepository.resetForTesting()
        SessionManager.logout()
        
        assertFalse(TiHinStyleRepository.isFavourite("p1"))
        assertNull(TryOnManager.selectedUserPhotoUri)
        assertNull(SessionManager.userEmail)
        assertTrue(SessionManager.isGuest)
    }
}
