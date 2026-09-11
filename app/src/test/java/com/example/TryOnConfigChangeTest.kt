package com.example

import com.example.credit.repository.CreditRepositoryProvider
import com.example.credit.repository.FakeCreditRepository
import com.example.tryon.service.LocalMockTryOnService
import com.example.tryon.service.TryOnServiceProvider
import com.example.tryon.model.GenerationStatus
import com.example.ui.TryOnManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TryOnConfigChangeTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        com.example.credit.repository.CreditRepositoryProvider.setForTesting(FakeCreditRepository())
        com.example.ui.SessionManager.resetForTesting(initialGuest = false, initialFree = 2, initialPurchased = 0)
        TryOnManager.resetForTesting()
        TryOnManager.selectedProductId = "test_product"
        TryOnManager.selectedUserPhotoUri = "file:///test/user.jpg"
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRecompositionOrConfigChange_doesNotStartSecondGeneration() = runTest {
        val req1 = TryOnManager.prepareRequest()
        val mockService = LocalMockTryOnService(delayMillis = 2000L)
        
        // Use a spy or just rely on GenerationStatus
        TryOnManager.startGeneration(request = req1, service = mockService)
        testDispatcher.scheduler.advanceTimeBy(100) // start processing
        
        val requestId = req1.requestId
        assertTrue(TryOnManager.generationStatus is GenerationStatus.Processing)
        
        // 2. Simulate configuration change or recomposition
        TryOnManager.startGeneration(request = req1, service = mockService)
        testDispatcher.scheduler.advanceTimeBy(100) // process any new coroutines
        
        // Assert: requestId remains the same, status is still Processing
        assertTrue(TryOnManager.generationStatus is GenerationStatus.Processing)
        val status2 = TryOnManager.generationStatus as GenerationStatus.Processing
        assertEquals(requestId, status2.requestId)
        
        TryOnManager.cancelGeneration()
    }
}
