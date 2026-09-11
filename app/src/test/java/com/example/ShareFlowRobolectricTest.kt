package com.example

import android.content.Intent
import com.example.data.repository.ProductRepositoryProvider
import com.example.share.data.SharedProductRepositoryProvider
import com.example.share.resolver.ProductResolverProvider
import com.example.share.ui.ShareFlowManager
import com.example.credit.repository.FakeCreditRepository
import com.example.credit.repository.CreditRepositoryProvider
import com.example.ui.SessionManager
import com.example.ui.TryOnManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ShareFlowRobolectricTest {

    private val controllers = mutableListOf<ActivityController<MainActivity>>()

    @Before
    fun setUp() {
        CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        ProductRepositoryProvider.reset()
        ProductResolverProvider.reset()
        SharedProductRepositoryProvider.reset()
        ShareFlowManager.resetForTesting()

        SessionManager.isGuest = false
        SessionManager.resetForTesting(initialFree = 2, initialPurchased = 0)
        SessionManager.releaseHeldCredit()
    }

    @After
    fun tearDown() {
        controllers.forEach { controller ->
            try {
                controller.pause().stop().destroy()
            } catch (_: Exception) {}
        }
        controllers.clear()

        ProductRepositoryProvider.reset()
        ProductResolverProvider.reset()
        SharedProductRepositoryProvider.reset()
        ShareFlowManager.resetForTesting()
    }

    private fun createActivity(intent: Intent): ActivityController<MainActivity> {
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        controllers.add(controller)
        return controller
    }

    @Test
    fun testColdStart_normalLauncherLaunchRemainsUnchanged() {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val initialCredits = SessionManager.credits
        val controller = createActivity(launcherIntent)
        controller.create().start().resume()

        // Verify normal launcher launch does not set any shared product error or try-on override
        assertNull(ShareFlowManager.lastSharedInput)
        assertEquals(initialCredits, SessionManager.credits)
        assertEquals(initialCredits, SessionManager.availableCredits)
    }

    @Test
    fun testColdStart_validShareDirectlyReachesTryOnWithProductPreselected() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.zara.com/in/en/cashmere-trench-1.html")
            putExtra(Intent.EXTRA_PACKAGE_NAME, "com.inditex.zara")
        }

        val initialCredits = SessionManager.credits
        val controller = createActivity(shareIntent)
        controller.create().start().resume()

        // 1. Direct route to TryOn with product preselected
        assertEquals("1", TryOnManager.selectedProductId)
        assertEquals("Oversized Cashmere Trench", TryOnManager.selectedProductName)
        assertEquals("ZARA", TryOnManager.selectedProductBrand)
        assertEquals(12999.0, TryOnManager.selectedProductPrice, 0.001)

        // 2. Saved shared product history created
        val recentShared = SharedProductRepositoryProvider.get().getRecentSharedProduct()
        assertNotNull(recentShared)
        assertEquals("1", recentShared!!.canonicalProductId)
        assertEquals("https://www.zara.com/in/en/cashmere-trench-1.html", recentShared.originalSharedUrl)

        // 3. Zero credits consumed for sharing and resolving
        assertEquals(initialCredits, SessionManager.credits)
        assertEquals(initialCredits, SessionManager.availableCredits)
        assertFalse(SessionManager.hasActiveHold)
    }

    @Test
    fun testColdStart_unsupportedShareDoesNotRouteToHomeSilently() {
        val unsupportedIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://unsupported-store-xyz.com/item/1234")
        }

        val initialCredits = SessionManager.credits
        val controller = createActivity(unsupportedIntent)
        controller.create().start().resume()

        // Graceful error state captured, not routing to home silently
        assertEquals("We couldn't recognize this product", ShareFlowManager.lastErrorMessage)
        assertEquals("https://unsupported-store-xyz.com/item/1234", ShareFlowManager.lastSharedInput?.sourceUrl)

        // Zero credits consumed
        assertEquals(initialCredits, SessionManager.credits)
        assertEquals(initialCredits, SessionManager.availableCredits)
    }

    @Test
    fun testOnNewIntent_processesShareWhileAppIsRunning() {
        // First start with launcher intent
        val launcherIntent = Intent(Intent.ACTION_MAIN)
        val controller = createActivity(launcherIntent)
        controller.create().start().resume()

        val initialCredits = SessionManager.credits

        // Now share a product from H&M while app is running
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check this overcoat: https://www.hm.com/product/2")
            putExtra(Intent.EXTRA_PACKAGE_NAME, "com.hm.goe")
        }

        controller.newIntent(shareIntent)

        // Successfully resolved and preselected H&M Wool Overcoat in TryOn
        assertEquals("2", TryOnManager.selectedProductId)
        assertEquals("Tailored Wool Overcoat", TryOnManager.selectedProductName)
        assertEquals("H&M", TryOnManager.selectedProductBrand)

        // Zero credits consumed
        assertEquals(initialCredits, SessionManager.credits)
    }
}
