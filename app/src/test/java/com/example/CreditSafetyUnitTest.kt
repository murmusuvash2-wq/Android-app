package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.credit.repository.CreditRepositoryProvider
import com.example.ui.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CreditSafetyUnitTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        CreditRepositoryProvider.init(context)
        SessionManager.init(context)
        SessionManager.resetForTesting(initialGuest = false, initialFree = 2, initialPurchased = 0)
    }

    @Test
    fun testAuthenticatedUser_initialCreditGrantIsTwo() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        assertEquals(2, repo.balanceFlow.value.total)
        assertEquals(2, repo.getAvailableCredits())
        assertTrue(repo.hasSufficientCredits())
    }

    @Test
    fun testGuestUser_initialCreditGrantIsZero() = runBlocking {
        SessionManager.setSessionMode(asGuest = true)
        kotlinx.coroutines.delay(100)
        val repo = CreditRepositoryProvider.get()
        assertEquals(2, repo.balanceFlow.value.total)
        assertEquals(0, repo.getAvailableCredits())
        assertTrue("Guests have unlimited client-side tries without credit hold", SessionManager.hasSufficientCredits)
    }

    @Test
    fun testHoldCredit_successWhenCreditsAvailable() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(5, 0)
        
        val opId = UUID.randomUUID().toString()
        val held = repo.hold(opId)

        assertTrue(held)
        assertEquals(5, repo.balanceFlow.value.total) // Total credits not decremented yet
        assertEquals(4, repo.getAvailableCredits()) // Available credits reflect hold
    }

    @Test
    fun testHoldCredit_idempotentOnRepeatedTaps() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(3, 0)
        val opId = UUID.randomUUID().toString()
        
        val firstHold = repo.hold(opId)
        val secondHold = repo.hold(opId) // double tap

        assertTrue("firstHold failed", firstHold)
        assertTrue("secondHold failed", secondHold)
        assertEquals(3, repo.balanceFlow.value.total)
        assertEquals(2, repo.getAvailableCredits()) // exactly 1 held, not 2
    }

    @Test
    fun testHoldCredit_failsWhenZeroCredits() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(0, 0)
        val opId = UUID.randomUUID().toString()
        val held = repo.hold(opId)

        assertFalse(held)
        assertFalse(repo.hasSufficientCredits())
        assertEquals(0, repo.getAvailableCredits())
    }

    @Test
    fun testConsumeHeldCredit_deductsExactlyOne() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(10, 0)
        val opId = UUID.randomUUID().toString()
        repo.hold(opId)

        val consumed = repo.consume(opId)

        assertTrue(consumed)
        assertEquals(9, repo.balanceFlow.value.total)
        assertEquals(9, repo.getAvailableCredits())
    }

    @Test
    fun testConsumeHeldCredit_cannotDoubleDeduct() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(10, 0)
        val opId = UUID.randomUUID().toString()
        repo.hold(opId)

        val firstConsume = repo.consume(opId)
        val secondConsume = repo.consume(opId)

        assertTrue("firstConsume failed", firstConsume)
        assertTrue("secondConsume failed", secondConsume) // Idempotent success
        assertEquals(9, repo.balanceFlow.value.total)
    }

    @Test
    fun testReleaseHeldCredit_restoresFullBalanceOnCancelOrFailure() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(7, 0)
        val opId = UUID.randomUUID().toString()
        repo.hold(opId)
        assertEquals(6, repo.getAvailableCredits())

        repo.release(opId)

        assertEquals(7, repo.balanceFlow.value.total)
        assertEquals(7, repo.getAvailableCredits())
    }

    @Test
    fun testGuestUser_neverHoldsOrDeductsCredits() = runBlocking {
        SessionManager.setSessionMode(asGuest = true)
        val repo = CreditRepositoryProvider.get()

        assertTrue(repo.hasSufficientCredits())
        val opId = UUID.randomUUID().toString()
        assertTrue(repo.hold(opId))
        
        assertTrue(repo.consume(opId))
        assertEquals(2, repo.balanceFlow.value.total)
    }

    @Test
    fun testNoNegativeBalance() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(1, 0)
        val opId = UUID.randomUUID().toString()
        repo.hold(opId)
        repo.consume(opId)

        assertEquals(0, repo.balanceFlow.value.total)

        // Attempting to consume without hold does nothing
        repo.consume(UUID.randomUUID().toString())
        assertEquals(0, repo.balanceFlow.value.total)
    }
}
