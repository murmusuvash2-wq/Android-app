package com.example.credit.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.credit.model.CreditBalance
import com.example.credit.model.CreditOperation
import com.example.credit.model.CreditSource
import com.example.credit.model.TransactionState
import com.example.data.local.AppDatabase
import com.example.data.local.CreditTransactionDao
import com.example.data.local.CreditTransactionEntity
import com.example.ui.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LocalCreditRepository(private val context: Context) : CreditRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences("tihin_credits_prefs", Context.MODE_PRIVATE)
    private val dao: CreditTransactionDao = AppDatabase.getInstance(context).creditTransactionDao()
    
    private val mutex = Mutex()
    private val _balanceFlow = MutableStateFlow(CreditBalance(0, 0))
    override val balanceFlow: StateFlow<CreditBalance> get() = _balanceFlow
    
    private var activeHoldsCount = 0
    private var isInitialized = false

    companion object {
        const val HOLD_EXPIRY_MILLIS = 5 * 60 * 1000L // 5 minutes
    }
    
    init {
        val free = prefs.getInt("free_credits", 2)
        val purchased = prefs.getInt("purchased_credits", 0)
        _balanceFlow.value = CreditBalance(free, purchased)
        
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock { initializeState() }
        }
    }
    
    private suspend fun initializeState() {
        if (isInitialized) return
        val heldTxs = dao.getHeldTransactions()
        val now = System.currentTimeMillis()
        var validHolds = 0
        for (tx in heldTxs) {
            if (now - tx.timestamp > HOLD_EXPIRY_MILLIS) {
                dao.insert(tx.copy(state = TransactionState.RELEASED.name, timestamp = now))
            } else {
                validHolds++
            }
        }
        activeHoldsCount = validHolds
        isInitialized = true
    }

    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            initializeState()
        }
    }
    
    private fun saveBalance(free: Int, purchased: Int) {
        prefs.edit()
            .putInt("free_credits", free)
            .putInt("purchased_credits", purchased)
            .apply()
        _balanceFlow.value = CreditBalance(free, purchased)
    }

    override suspend fun getAvailableCredits(): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureInitialized()
            val total = _balanceFlow.value.total
            if (SessionManager.isGuest) 0 else (total - activeHoldsCount).coerceAtLeast(0)
        }
    }

    override suspend fun hasSufficientCredits(): Boolean = withContext(Dispatchers.IO) {
        SessionManager.isGuest || getAvailableCredits() >= 1
    }

    override suspend fun hold(operationId: String): Boolean = withContext(Dispatchers.IO) {
        if (SessionManager.isGuest) return@withContext true
        
        mutex.withLock {
            ensureInitialized()
            val existing = dao.getTransaction(operationId)
            if (existing != null) {
                return@withLock existing.state == TransactionState.HELD.name
            }
            
            val total = _balanceFlow.value.total
            val available = (total - activeHoldsCount).coerceAtLeast(0)
            
            if (available >= 1) {
                val balance = _balanceFlow.value
                val source = if (balance.freeCredits > 0) CreditSource.FREE else CreditSource.PURCHASED
                
                val entity = CreditTransactionEntity(
                    operationId = operationId,
                    amount = 1,
                    state = TransactionState.HELD.name,
                    source = source.name,
                    timestamp = System.currentTimeMillis()
                )
                dao.insert(entity)
                activeHoldsCount++
                return@withLock true
            }
            return@withLock false
        }
    }

    override suspend fun consume(operationId: String): Boolean = withContext(Dispatchers.IO) {
        if (SessionManager.isGuest) return@withContext true
        
        mutex.withLock {
            ensureInitialized()
            val existing = dao.getTransaction(operationId) ?: return@withLock false
            if (existing.state == TransactionState.CONSUMED.name) return@withLock true
            if (existing.state != TransactionState.HELD.name) return@withLock false
            
            var free = _balanceFlow.value.freeCredits
            var purchased = _balanceFlow.value.purchasedCredits
            
            if (existing.source == CreditSource.FREE.name) {
                if (free >= 1) free -= 1 else return@withLock false
            } else {
                if (purchased >= 1) purchased -= 1 else return@withLock false
            }
            
            saveBalance(free, purchased)
            
            val updated = existing.copy(state = TransactionState.CONSUMED.name, timestamp = System.currentTimeMillis())
            dao.insert(updated)
            
            if (activeHoldsCount > 0) activeHoldsCount--
            return@withLock true
        }
    }

    override suspend fun release(operationId: String): Boolean = withContext(Dispatchers.IO) {
        if (SessionManager.isGuest) return@withContext true
        
        mutex.withLock {
            ensureInitialized()
            val existing = dao.getTransaction(operationId) ?: return@withLock true
            if (existing.state == TransactionState.RELEASED.name) return@withLock true
            if (existing.state != TransactionState.HELD.name) return@withLock false
            
            val updated = existing.copy(state = TransactionState.RELEASED.name, timestamp = System.currentTimeMillis())
            dao.insert(updated)
            
            if (activeHoldsCount > 0) activeHoldsCount--
            return@withLock true
        }
    }

    override suspend fun getTransactionHistory(): List<CreditOperation> = withContext(Dispatchers.IO) {
        dao.getAllTransactions().map {
            CreditOperation(
                operationId = it.operationId,
                amount = it.amount,
                state = TransactionState.valueOf(it.state),
                source = CreditSource.valueOf(it.source),
                timestamp = it.timestamp
            )
        }
    }

    override suspend fun addPurchasedCredits(amount: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _balanceFlow.value
            saveBalance(current.freeCredits, current.purchasedCredits + amount)
        }
    }

    override suspend fun setBalance(free: Int, purchased: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            saveBalance(free, purchased)
        }
    }

    override suspend fun resetForTesting(free: Int, purchased: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            dao.clear()
            activeHoldsCount = 0
            isInitialized = true
            saveBalance(free, purchased)
        }
    }
}
