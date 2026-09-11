package com.example.credit.repository

import com.example.credit.model.CreditBalance
import com.example.credit.model.CreditOperation
import com.example.ui.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeCreditRepository : CreditRepository {
    private val _balanceFlow = MutableStateFlow(CreditBalance(2, 0))
    override val balanceFlow: StateFlow<CreditBalance> get() = _balanceFlow

    private var activeHolds = 0

    override suspend fun getAvailableCredits(): Int {
        val total = _balanceFlow.value.total
        return if (SessionManager.isGuest) 0 else (total - activeHolds).coerceAtLeast(0)
    }

    override suspend fun hasSufficientCredits(): Boolean {
        return SessionManager.isGuest || getAvailableCredits() >= 1
    }

    override suspend fun hold(operationId: String): Boolean {
        if (SessionManager.isGuest) return true
        if (getAvailableCredits() >= 1) {
            activeHolds++
            return true
        }
        return false
    }

    override suspend fun consume(operationId: String): Boolean {
        if (SessionManager.isGuest) return true
        if (activeHolds > 0) {
            val free = _balanceFlow.value.freeCredits
            val purchased = _balanceFlow.value.purchasedCredits
            if (free > 0) {
                _balanceFlow.value = CreditBalance(free - 1, purchased)
            } else if (purchased > 0) {
                _balanceFlow.value = CreditBalance(free, purchased - 1)
            }
            activeHolds--
            return true
        }
        return false
    }

    override suspend fun release(operationId: String): Boolean {
        if (SessionManager.isGuest) return true
        if (activeHolds > 0) {
            activeHolds--
            return true
        }
        return false
    }

    override suspend fun getTransactionHistory(): List<CreditOperation> = emptyList()

    override suspend fun addPurchasedCredits(amount: Int) {
        val current = _balanceFlow.value
        _balanceFlow.value = CreditBalance(current.freeCredits, current.purchasedCredits + amount)
    }

    override suspend fun setBalance(free: Int, purchased: Int) {
        _balanceFlow.value = CreditBalance(free, purchased)
    }

    override suspend fun resetForTesting(free: Int, purchased: Int) {
        activeHolds = 0
        _balanceFlow.value = CreditBalance(free, purchased)
    }
}
