package com.example



import android.content.Intent
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import com.example.data.repository.ProductRepositoryProvider
import com.example.share.data.DefaultSharedProductRepository
import com.example.share.data.SharedProductRepository
import com.example.share.data.SharedProductRepositoryProvider
import com.example.share.merchant.DefaultMerchantLinkProvider
import com.example.share.merchant.MerchantLinkProvider
import com.example.share.model.SharedProductInput
import com.example.share.receiver.ShareIntentHandler
import com.example.share.receiver.ShareIntentResult
import com.example.share.resolver.DefaultProductResolver
import com.example.share.resolver.ProductResolutionResult
import com.example.share.resolver.ProductResolver
import com.example.share.resolver.ProductResolverProvider
import com.example.share.ui.ShareFlowManager
import com.example.share.ui.ShareResolutionOutcome
import com.example.ui.SessionManager
import com.example.credit.repository.CreditRepositoryProvider
import com.example.ui.TryOnManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ShareFoundationUnitTest {

    private lateinit var resolver: ProductResolver
    private lateinit var sharedRepo: SharedProductRepository
    private val catalog = ProductRepository.get()

    @Before
    fun setUp() {
        
        ProductRepositoryProvider.reset()
        ProductResolverProvider.reset()
        SharedProductRepositoryProvider.reset()
        ShareFlowManager.resetForTesting()

        resolver = ProductResolverProvider.get()
        sharedRepo = SharedProductRepositoryProvider.get()

        SessionManager.isGuest = false
        SessionManager.resetForTesting(initialFree = 5, initialPurchased = 0)
        SessionManager.releaseHeldCredit()
    }

    @After
    fun tearDown() {
        ProductRepositoryProvider.reset()
        ProductResolverProvider.reset()
        SharedProductRepositoryProvider.reset()
        ShareFlowManager.resetForTesting()
    }

    // ==========================================
    // 1. ShareIntentHandler Extraction Tests
    // ==========================================

    @Test
    fun testShareIntentHandler_validDirectUrl() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.zara.com/in/en/cashmere-trench-1.html")
            putExtra(Intent.EXTRA_PACKAGE_NAME, "com.inditex.zara")
        }

        val result = ShareIntentHandler.extract(intent)
        assertTrue(result is ShareIntentResult.ValidTextShare)
        val valid = result as ShareIntentResult.ValidTextShare
        assertEquals("https://www.zara.com/in/en/cashmere-trench-1.html", valid.input.sourceUrl)
        assertEquals("com.inditex.zara", valid.input.sourcePackageName)
    }

    @Test
    fun testShareIntentHandler_sharedTextContainingValidUrl() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Hey, check out this dress on Myntra! https://www.myntra.com/dresses/zara/1.html It looks amazing."
            )
            putExtra(Intent.EXTRA_PACKAGE_NAME, "com.myntra.android")
        }

        val result = ShareIntentHandler.extract(intent)
        assertTrue(result is ShareIntentResult.ValidTextShare)
        val valid = result as ShareIntentResult.ValidTextShare
        assertEquals("https://www.myntra.com/dresses/zara/1.html", valid.input.sourceUrl)
        assertEquals("com.myntra.android", valid.input.sourcePackageName)
        assertTrue(valid.input.sharedText!!.contains("Hey, check out this dress"))
    }

    @Test
    fun testShareIntentHandler_emptyAndNullPayload() {
        val nullTextIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
        }
        val emptyResult = ShareIntentHandler.extract(nullTextIntent)
        assertTrue(emptyResult is ShareIntentResult.Empty)

        val blankTextIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "   ")
        }
        val blankResult = ShareIntentHandler.extract(blankTextIntent)
        assertTrue(blankResult is ShareIntentResult.Empty)

        val nullIntentResult = ShareIntentHandler.extract(null)
        assertTrue(nullIntentResult is ShareIntentResult.NotShareIntent)
    }

    @Test
    fun testShareIntentHandler_malformedContentWithoutUrl() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Just a casual text message with no URL at all")
        }

        val result = ShareIntentHandler.extract(intent)
        assertTrue(result is ShareIntentResult.Malformed)
        assertEquals("Just a casual text message with no URL at all", (result as ShareIntentResult.Malformed).rawContent)
    }

    @Test
    fun testShareIntentHandler_nonShareActionReturnsNotShareIntent() {
        val mainIntent = Intent(Intent.ACTION_MAIN)
        assertEquals(ShareIntentResult.NotShareIntent, ShareIntentHandler.extract(mainIntent))

        val viewIntent = Intent(Intent.ACTION_VIEW)
        assertEquals(ShareIntentResult.NotShareIntent, ShareIntentHandler.extract(viewIntent))
    }

    // ==========================================
    // 2. ProductResolver & Merchant Adapters
    // ==========================================

    @Test
    fun testResolver_resolvesSupportedMerchantsToCanonicalProducts() {
        // Zara -> Canonical Product "1"
        val zaraResult = resolver.resolve(
            SharedProductInput("https://www.zara.com/in/en/cashmere-trench-1.html", "com.inditex.zara")
        )
        assertTrue(zaraResult is ProductResolutionResult.Success)
        val zaraSuccess = zaraResult as ProductResolutionResult.Success
        assertEquals("1", zaraSuccess.product.id)
        assertEquals("Oversized Cashmere Trench", zaraSuccess.product.name)
        assertEquals("ZARA", zaraSuccess.product.brand)
        assertEquals("ZARA", zaraSuccess.merchantName)

        // H&M -> Canonical Product "2"
        val hnmResult = resolver.resolve(
            SharedProductInput("https://www2.hm.com/en_in/productpage.2.html", "com.hm.goe")
        )
        assertTrue(hnmResult is ProductResolutionResult.Success)
        assertEquals("2", (hnmResult as ProductResolutionResult.Success).product.id)
        assertEquals("Tailored Wool Overcoat", hnmResult.product.name)

        // Mango -> Canonical Product "3"
        val mangoResult = resolver.resolve(
            SharedProductInput("https://shop.mango.com/in/women/blazers/linen-blazer_3.html")
        )
        assertTrue(mangoResult is ProductResolutionResult.Success)
        assertEquals("3", (mangoResult as ProductResolutionResult.Success).product.id)

        // Urbanic -> Canonical Product "5"
        val urbanicResult = resolver.resolve(
            SharedProductInput("https://www.urbanic.com/product/emerald-satin-maxi?id=5")
        )
        assertTrue(urbanicResult is ProductResolutionResult.Success)
        assertEquals("5", (urbanicResult as ProductResolutionResult.Success).product.id)

        // ASOS -> Canonical Product "6"
        val asosResult = resolver.resolve(
            SharedProductInput("https://www.asos.com/prd/6?clr=pastel")
        )
        assertTrue(asosResult is ProductResolutionResult.Success)
        assertEquals("6", (asosResult as ProductResolutionResult.Success).product.id)
    }

    @Test
    fun testResolver_resolvesIndianECommerceMarketplaces() {
        // Myntra
        val myntraResult = resolver.resolve(
            SharedProductInput(
                "https://www.myntra.com/dresses/zara/1",
                "com.myntra.android",
                "Check out Zara Cashmere Trench on Myntra"
            )
        )
        assertTrue(myntraResult is ProductResolutionResult.Success)
        assertEquals("1", (myntraResult as ProductResolutionResult.Success).product.id)

        // Amazon
        val amazonResult = resolver.resolve(
            SharedProductInput(
                "https://www.amazon.in/dp/2?tag=affiliate_test",
                "com.amazon.mShop.android.shopping"
            )
        )
        assertTrue(amazonResult is ProductResolutionResult.Success)
        assertEquals("2", (amazonResult as ProductResolutionResult.Success).product.id)

        // Flipkart
        val flipkartResult = resolver.resolve(
            SharedProductInput(
                "https://www.flipkart.com/minimalist-linen-blazer/p/itm123?pid=3",
                "com.flipkart.android"
            )
        )
        assertTrue(flipkartResult is ProductResolutionResult.Success)
        assertEquals("3", (flipkartResult as ProductResolutionResult.Success).product.id)

        // Ajio
        val ajioResult = resolver.resolve(
            SharedProductInput(
                "https://www.ajio.com/urbanic-emerald-satin-maxi/p/5",
                "com.ril.ajio"
            )
        )
        assertTrue(ajioResult is ProductResolutionResult.Success)
        assertEquals("5", (ajioResult as ProductResolutionResult.Success).product.id)
    }

    @Test
    fun testResolver_resolvesLegacyAliasGracefully() {
        val aliasResult = resolver.resolve(
            SharedProductInput("https://example.com/item?id=p4")
        )
        assertTrue(aliasResult is ProductResolutionResult.Success)
        assertEquals("4", (aliasResult as ProductResolutionResult.Success).product.id)
        assertEquals("Structured Oxford & Trousers", aliasResult.product.name)
    }

    @Test
    fun testResolver_unsupportedMerchantReturnsUnsupportedFailure() {
        val unsupportedInput = SharedProductInput("https://unknown-shop-never-heard-of.xyz/item/999")
        val result = resolver.resolve(unsupportedInput)

        assertTrue(result is ProductResolutionResult.Failure.UnsupportedMerchant)
        val failure = result as ProductResolutionResult.Failure.UnsupportedMerchant
        assertEquals("https://unknown-shop-never-heard-of.xyz/item/999", failure.url)
        assertEquals("We couldn't recognize this product", failure.message)
    }

    @Test
    fun testResolver_emptyUrlReturnsEmptyFailure() {
        val emptyInput = SharedProductInput("")
        val result = resolver.resolve(emptyInput)

        assertTrue(result is ProductResolutionResult.Failure.EmptyInput)
        assertEquals("We couldn't recognize this product", (result as ProductResolutionResult.Failure).message)
    }

    // ==========================================
    // 3. Merchant Link Foundation
    // ==========================================

    @Test
    fun testMerchantLinkProvider_preservesOriginalUrlUntouched() {
        val provider: MerchantLinkProvider = DefaultMerchantLinkProvider
        val original = "https://www.zara.com/in/en/oversized-cashmere-trench-p1.html?color=beige&utm_source=test"

        val resolved = provider.getPurchaseUrl(original, "ZARA")
        assertEquals(original, resolved)
    }

    // ==========================================
    // 4. Shared Product Persistence Foundation
    // ==========================================

    @Test
    fun testSharedProductRepository_savesAndRetrievesRecord() {
        val product1 = catalog.getProductById("1")!!
        val originalUrl = "https://www.zara.com/product/1"

        val savedRecord = sharedRepo.saveSharedProduct(
            product = product1,
            originalUrl = originalUrl,
            merchantName = "ZARA"
        )

        assertNotNull(savedRecord.id)
        assertEquals("1", savedRecord.canonicalProductId)
        assertEquals("Oversized Cashmere Trench", savedRecord.productName)
        assertEquals("ZARA", savedRecord.merchantName)
        assertEquals(originalUrl, savedRecord.originalSharedUrl)
        assertEquals(product1.primaryImageUrl, savedRecord.productImage)
        assertEquals(12999.0, savedRecord.price!!, 0.001)

        val recent = sharedRepo.getRecentSharedProduct()
        assertNotNull(recent)
        assertEquals(savedRecord.id, recent!!.id)
        assertEquals("1", recent.canonicalProductId)

        val byCanonical = sharedRepo.getSharedRecordForProduct("1")
        assertNotNull(byCanonical)
        assertEquals(savedRecord.id, byCanonical!!.id)
    }

    @Test
    fun testSharedProductRepository_duplicateSharedProductBehavior() {
        val product1 = catalog.getProductById("1")!!
        val product2 = catalog.getProductById("2")!!

        sharedRepo.saveSharedProduct(product1, "https://zara.com/link1", "ZARA", timestamp = 1000L)
        sharedRepo.saveSharedProduct(product2, "https://hm.com/link2", "H&M", timestamp = 2000L)

        assertEquals(2, sharedRepo.getSharedProducts().size)
        assertEquals("2", sharedRepo.getRecentSharedProduct()!!.canonicalProductId)

        // Reshare product 1 with a newer timestamp and link
        sharedRepo.saveSharedProduct(product1, "https://zara.com/newer_link", "ZARA", timestamp = 3000L)

        val list = sharedRepo.getSharedProducts()
        assertEquals("Duplicate share updates existing record and does not duplicate", 2, list.size)
        assertEquals("Reshared item moves to front of recent history", "1", list.first().canonicalProductId)
        assertEquals("https://zara.com/newer_link", list.first().originalSharedUrl)
        assertEquals(3000L, list.first().resolvedAt)
    }

    // ==========================================
    // 5. Credit Safety During Share & Resolution
    // ==========================================

    @Test
    fun testShareAndResolution_consumesZeroCredits() {
        val initialCredits = SessionManager.credits
        val initialAvailable = SessionManager.availableCredits
        assertEquals(5, initialCredits)

        // 1. Process valid share intent
        val validIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://www.zara.com/product/1")
        }
        val outcome = ShareFlowManager.processIntent(validIntent)
        assertTrue(outcome is ShareResolutionOutcome.GoToTryOn)

        // Verify credit state remains untouched
        assertEquals(initialCredits, SessionManager.credits)
        assertEquals(initialAvailable, SessionManager.availableCredits)
        assertFalse(SessionManager.hasActiveHold)

        // 2. Preselected product in TryOnManager
        assertEquals("1", TryOnManager.selectedProductId)
        assertEquals("Oversized Cashmere Trench", TryOnManager.selectedProductName)

        // 3. Process unsupported share intent
        val unsupportedIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://unsupported.com/random")
        }
        val errorOutcome = ShareFlowManager.processIntent(unsupportedIntent)
        assertTrue(errorOutcome is ShareResolutionOutcome.ShowError)

        // Verify credit state still completely untouched
        assertEquals(initialCredits, SessionManager.credits)
        assertEquals(initialAvailable, SessionManager.availableCredits)
        assertFalse(SessionManager.hasActiveHold)

        // 4. Retry resolution
        val retryOutcome = ShareFlowManager.retryResolution()
        assertTrue(retryOutcome is ShareResolutionOutcome.ShowError)
        assertEquals(initialCredits, SessionManager.credits)
        assertEquals(initialAvailable, SessionManager.availableCredits)
    }
}
