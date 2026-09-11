package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.credit.model.CreditSource
import com.example.credit.model.TransactionState
import com.example.credit.repository.CreditRepositoryProvider
import com.example.credit.repository.LocalCreditRepository
import com.example.data.local.AppDatabase
import com.example.ui.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
class CreditConcurrencyUnitTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        CreditRepositoryProvider.init(context)
        SessionManager.init(context)
        SessionManager.resetForTesting(initialGuest = false, initialFree = 1, initialPurchased = 0)
    }

    @Test
    fun testConcurrentHolds_onlyOneSucceeds() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(1, 0)
        assertEquals(1, repo.getAvailableCredits())

        val opId1 = UUID.randomUUID().toString()
        val opId2 = UUID.randomUUID().toString()

        val results = listOf(
            async(Dispatchers.IO) { repo.hold(opId1) },
            async(Dispatchers.IO) { repo.hold(opId2) }
        ).awaitAll()

        val successCount = results.count { it }
        assertEquals("Only one hold should succeed", 1, successCount)
        assertEquals(0, repo.getAvailableCredits())
    }

    @Test
    fun testConcurrentConsumes_onlyOneDeducts() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(1, 0)
        val opId = UUID.randomUUID().toString()
        assertTrue(repo.hold(opId))
        
        val results = listOf(
            async(Dispatchers.IO) { repo.consume(opId) },
            async(Dispatchers.IO) { repo.consume(opId) }
        ).awaitAll()

        // Both might return true (idempotent), but balance should only be deducted once.
        assertEquals(0, repo.balanceFlow.value.total)
    }

    @Test
    fun testConcurrentReleases_onlyOneReleases() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(1, 0)
        val opId = UUID.randomUUID().toString()
        assertTrue(repo.hold(opId))
        assertEquals(0, repo.getAvailableCredits())
        
        val results = listOf(
            async(Dispatchers.IO) { repo.release(opId) },
            async(Dispatchers.IO) { repo.release(opId) }
        ).awaitAll()

        // Both might return true, but available credits should only go up by 1
        assertEquals(1, repo.getAvailableCredits())
        assertEquals(1, repo.balanceFlow.value.total)
    }

    @Test
    fun testProcessRestart_releasesStaleHolds() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = AppDatabase.getInstance(context)
        
        // Insert a stale hold manually
        val staleOpId = UUID.randomUUID().toString()
        db.creditTransactionDao().insert(
            com.example.data.local.CreditTransactionEntity(
                operationId = staleOpId,
                amount = 1,
                state = TransactionState.HELD.name,
                source = CreditSource.FREE.name,
                timestamp = System.currentTimeMillis() - LocalCreditRepository.HOLD_EXPIRY_MILLIS - 1000
            )
        )
        
        // Re-create repository to simulate restart
        val newRepo = LocalCreditRepository(context)
        newRepo.setBalance(1, 0)
        
        // Give it a moment to initialize
        val available = newRepo.getAvailableCredits()
        
        // The stale hold should be released, making the 1 credit available
        assertEquals(1, available)
        val tx = db.creditTransactionDao().getTransaction(staleOpId)
        assertEquals(TransactionState.RELEASED.name, tx?.state)
    }

    @Test
    fun testNegativeBalance_neverOccurs() = runBlocking {
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(0, 0)
        val opId = UUID.randomUUID().toString()
        
        assertFalse(repo.hold(opId))
        assertEquals(0, repo.balanceFlow.value.total)
    }

    @Test
    fun testAuthCreditReset_returningAccountDoesNotGetFreeCredits() = runBlocking {
        // Assume user was logged in and has 0 credits
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(0, 5) // 5 purchased
        
        val ctx = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Ensure they have received initial grant
        ctx.getSharedPreferences("tihin_session_prefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putBoolean("has_received_initial_grant", true)
            .apply()
        
        // Emulate what would happen on log back in (simulate a backend restoring credits)
        SessionManager.setSessionMode(asGuest = true) // logs out, wipes DB to 0,0
        repo.setBalance(0, 5) // simulated backend restore
        SessionManager.setSessionMode(asGuest = false) // logs back in
        
        kotlinx.coroutines.delay(100)
        
        // Should NOT get 2 free credits since they already received initial grant
        assertEquals(0, SessionManager.freeCredits)
        assertEquals(5, SessionManager.purchasedCredits)
    }

    @Test
    fun testCancellation_releasesCredit() = runBlocking {
        // This is tricky to test fully here without the UI but we can test the repo logic.
        val repo = CreditRepositoryProvider.get()
        repo.setBalance(1, 0)
        val opId = UUID.randomUUID().toString()
        assertTrue(repo.hold(opId))
        assertEquals(0, repo.getAvailableCredits())
        
        // Cancel logic
        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
            repo.release(opId)
        }
        
        assertEquals(1, repo.getAvailableCredits())
    }
}
