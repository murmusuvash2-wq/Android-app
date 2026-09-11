package com.example

import com.example.data.repository.ProductRepositoryProvider
import com.example.share.model.SharedProductInput
import com.example.share.resolver.ProductResolutionResult
import com.example.share.resolver.ProductResolverProvider
import com.example.tryon.model.GenerationStatus
import com.example.tryon.model.TryOnError
import com.example.tryon.model.TryOnErrorCategory
import com.example.tryon.model.TryOnException
import com.example.tryon.model.TryOnRequest
import com.example.tryon.model.TryOnResult
import com.example.tryon.model.UserPhotoMetadata
import com.example.tryon.service.LocalMockTryOnService
import com.example.tryon.service.TryOnServiceProvider
import com.example.ui.OnMeStyleRepository
import com.example.ui.SessionManager
import com.example.ui.TryOnManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import androidx.test.core.app.ApplicationProvider
import com.example.credit.repository.CreditRepositoryProvider
import com.example.credit.repository.FakeCreditRepository
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class TryOnBackendReadyInterfaceTest {

    @Before
    fun setUp() {
        com.example.credit.repository.CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        ProductRepositoryProvider.reset()
        ProductResolverProvider.reset()
        TryOnServiceProvider.reset()
        TryOnManager.resetForTesting()

        // SessionManager.init
        SessionManager.resetForTesting(initialGuest = false, initialFree = 2, initialPurchased = 0)
    }

    @After
    fun tearDown() {
        ProductRepositoryProvider.reset()
        ProductResolverProvider.reset()
        TryOnServiceProvider.reset()
        TryOnManager.resetForTesting()
        SessionManager.resetForTesting()
    }

    @Test
    fun testTryOnRequest_creationAndValidation() {
        val reqId = UUID.randomUUID().toString()
        val request = TryOnRequest(
            requestId = reqId,
            productId = "1",
            userPhotoUri = "file:///photos/my_look.jpg",
            userPhotoMetadata = UserPhotoMetadata(uri = "file:///photos/my_look.jpg", source = "gallery")
        )

        assertEquals(reqId, request.requestId)
        assertEquals("1", request.productId)
        assertEquals("file:///photos/my_look.jpg", request.userPhotoUri)
        assertEquals("gallery", request.userPhotoMetadata?.source)
        assertTrue(request.createdAt > 0)

        // Blank product ID throws IllegalArgumentException
        try {
            TryOnRequest(productId = "", userPhotoUri = "file:///photos/look.jpg")
            fail("Expected exception on blank productId")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("productId"))
        }

        // Blank photo URI throws IllegalArgumentException
        try {
            TryOnRequest(productId = "1", userPhotoUri = "  ")
            fail("Expected exception on blank userPhotoUri")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("userPhotoUri"))
        }

        // Blank request ID throws IllegalArgumentException
        try {
            TryOnRequest(requestId = "", productId = "1", userPhotoUri = "file:///photos/look.jpg")
            fail("Expected exception on blank requestId")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("requestId"))
        }
    }

    @Test
    fun testTryOnResult_creationAndValidation() {
        val reqId = UUID.randomUUID().toString()
        val result = TryOnResult(
            requestId = reqId,
            generatedImageUri = "https://example.com/result.jpg",
            productId = "2",
            generationTimestamp = 1000L,
            watermarkApplied = true
        )

        assertEquals(reqId, result.requestId)
        assertEquals("https://example.com/result.jpg", result.generatedImageUri)
        assertEquals("2", result.productId)
        assertEquals(1000L, result.generationTimestamp)
        assertTrue(result.watermarkApplied)

        // Blank generatedImageUri throws
        try {
            TryOnResult(requestId = reqId, generatedImageUri = "", productId = "2")
            fail("Expected exception on blank generatedImageUri")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("generatedImageUri"))
        }
    }

    @Test
    fun testLocalMockTryOnService_successReturnsDeterministicResult() = runBlocking {
        val service = LocalMockTryOnService(delayMillis = 0L)
        val request = TryOnRequest(
            productId = "1",
            userPhotoUri = "file:///test/user.jpg"
        )

        val result = service.generate(request)

        assertEquals(request.requestId, result.requestId)
        assertEquals("1", result.productId)
        assertFalse(result.generatedImageUri.isBlank())
        assertTrue(result.watermarkApplied)
    }

    @Test
    fun testLocalMockTryOnService_failureThrowsTryOnException() = runBlocking {
        val customError = TryOnError.GenerationFailed("AI model overloaded")
        val service = LocalMockTryOnService(
            delayMillis = 0L,
            shouldFail = true,
            failureError = customError
        )
        val request = TryOnRequest(
            productId = "1",
            userPhotoUri = "file:///test/user.jpg"
        )

        try {
            service.generate(request)
            fail("Expected TryOnException")
        } catch (e: TryOnException) {
            assertEquals(TryOnErrorCategory.GENERATION_FAILED, e.error.category)
            assertEquals("We couldn't generate your look right now. Please try again.", e.error.userMessage)
            assertEquals("AI model overloaded", e.error.technicalDetails)
        }
    }

    @Test
    fun testLocalMockTryOnService_cancellationIsIdempotent() = runBlocking {
        val service = LocalMockTryOnService(delayMillis = 0L)
        val request = TryOnRequest(
            productId = "1",
            userPhotoUri = "file:///test/user.jpg"
        )

        // Cancel before generate
        service.cancel(request.requestId)
        // Idempotent repeated cancel
        service.cancel(request.requestId)

        try {
            service.generate(request)
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            assertTrue(e.message!!.contains(request.requestId))
        }
    }

    @Test
    fun testServiceDoesNotOwnOrDeductCredits() = runBlocking {
        val service = LocalMockTryOnService(delayMillis = 0L)
        val initialCredits = SessionManager.credits
        val initialAvailable = SessionManager.availableCredits

        val request = TryOnRequest(
            productId = "1",
            userPhotoUri = "file:///test/user.jpg"
        )

        // Calling service directly should not touch credits at all
        service.generate(request)

        assertEquals(initialCredits, SessionManager.credits)
        assertEquals(initialAvailable, SessionManager.availableCredits)
        kotlinx.coroutines.delay(100)
        assertFalse(SessionManager.hasActiveHold)
    }

    @Test
    fun testOrchestration_successConsumesHeldCredit() = runBlocking {
        val fastService = LocalMockTryOnService(delayMillis = 0L)
        TryOnServiceProvider.setForTesting(fastService)

        TryOnManager.selectedProductId = "1"
        TryOnManager.selectedUserPhotoUri = "file:///test/user.jpg"

        // 2. Prepare request and execute generation
        val request = TryOnManager.prepareRequest()
        val result = TryOnManager.executeGeneration(request, fastService)

        // 3. Verifications
        assertEquals(request.requestId, result.requestId)
        assertEquals("1", result.productId)
        assertEquals(result.generatedImageUri, TryOnManager.generatedResultImageUri)
        assertTrue(TryOnManager.generationStatus is GenerationStatus.Success)

        // 4. Exactly 1 credit consumed, hold cleared, remaining 1
        kotlinx.coroutines.delay(100)
        assertEquals(1, SessionManager.credits)
        assertEquals(1, SessionManager.availableCredits)
        kotlinx.coroutines.delay(100)
        assertFalse(SessionManager.hasActiveHold)
    }

    @Test
    fun testOrchestration_failedGenerationReleasesHeldCredit() = runBlocking {
        val failingService = LocalMockTryOnService(
            delayMillis = 0L,
            shouldFail = true,
            failureError = TryOnError.Timeout
        )

        TryOnManager.selectedProductId = "1"
        TryOnManager.selectedUserPhotoUri = "file:///test/user.jpg"

        val request = TryOnManager.prepareRequest()

        try {
            TryOnManager.executeGeneration(request, failingService)
            fail("Expected TryOnException")
        } catch (e: TryOnException) {
            assertEquals(TryOnErrorCategory.TIMEOUT, e.error.category)
        }

        // Held credit released, balance remains 2, available remains 2
        assertEquals(2, SessionManager.credits)
        assertEquals(2, SessionManager.availableCredits)
        assertTrue(TryOnManager.generationStatus is GenerationStatus.Failed)
    }

    @Test
    fun testOrchestration_cancelledGenerationReleasesHeldCredit() = runBlocking {
        val fastService = LocalMockTryOnService(delayMillis = 0L)
        TryOnManager.selectedProductId = "1"
        TryOnManager.selectedUserPhotoUri = "file:///test/user.jpg"

        val req = TryOnManager.prepareRequest()
        CreditRepositoryProvider.get().hold(req.requestId)
        // Simulate processing state
        TryOnManager.generationStatus = GenerationStatus.Processing(TryOnManager.currentRequest!!.requestId)

        // Cancel generation
        TryOnManager.cancelGeneration(fastService)

        // Held credit released, balance preserved
        assertEquals(2, SessionManager.credits)
        assertEquals(2, SessionManager.availableCredits)
        kotlinx.coroutines.delay(100)
        assertFalse(SessionManager.hasActiveHold)
        assertTrue(TryOnManager.generationStatus is GenerationStatus.Cancelled)

        // Idempotent repeated cancel
        TryOnManager.cancelGeneration(fastService)
        assertEquals(2, SessionManager.credits)
        assertEquals(2, SessionManager.availableCredits)
        kotlinx.coroutines.delay(100)
        assertFalse(SessionManager.hasActiveHold)
    }

    @Test
    fun testShareToUniversalTryOn_stillPreselectsProductForTryOnRequest() = runBlocking {
        // Resolve shared product from Zara
        val input = SharedProductInput(
            sourceUrl = "https://www.zara.com/in/en/cashmere-trench-1.html",
            sourcePackageName = "com.inditex.zara"
        )
        val outcome = ProductResolverProvider.get().resolve(input)
        val resolved = (outcome as ProductResolutionResult.Success).product

        TryOnManager.selectProduct(resolved, input.sourceUrl)
        TryOnManager.updateUserPhoto("file:///test/my_photo.jpg")

        // Prepare domain request
        val request = TryOnManager.prepareRequest()

        assertEquals("1", request.productId)
        assertEquals("file:///test/my_photo.jpg", request.userPhotoUri)
        assertEquals("Oversized Cashmere Trench", TryOnManager.selectedProductName)
        assertEquals("ZARA", TryOnManager.selectedProductBrand)

        // Zero credits consumed for share or request prep
        assertEquals(2, SessionManager.credits)
        assertEquals(2, SessionManager.availableCredits)
        kotlinx.coroutines.delay(100)
        assertFalse(SessionManager.hasActiveHold)
    }

    @Test
    fun testSavedResultIntegration_remainsCompatibleWithOnMeStyleRepository() = runBlocking {
        val fastService = LocalMockTryOnService(delayMillis = 0L)
        val request = TryOnRequest(
            productId = "1",
            userPhotoUri = "file:///test/user.jpg"
        )
        val result = fastService.generate(request)

        // Map domain TryOnResult to UI / persistence TryOnResult
        val savedResult = com.example.ui.TryOnResult(
            id = "saved_${result.requestId}",
            userPhoto = request.userPhotoUri,
            outfitImage = "https://example.com/outfit.jpg",
            resultImage = result.generatedImageUri,
            createdAt = "Just now",
            isFavourite = false,
            productId = result.productId,
            productName = "Oversized Cashmere Trench",
            productBrand = "ZARA",
            productPrice = 12999.0,
            cardHeight = 240
        )

        OnMeStyleRepository.saveResult(savedResult)

        // Verify result exists and is at top of saved list
        val topResult = OnMeStyleRepository.savedResults.firstOrNull()
        assertNotNull(topResult)
        assertEquals("saved_${result.requestId}", topResult!!.id)
        assertEquals("1", topResult.productId)
        assertEquals(result.generatedImageUri, topResult.resultImage)
    }
}
