package com.example.credit.repository

import com.example.credit.model.CreditBalance
import com.example.credit.model.CreditOperation
import kotlinx.coroutines.flow.StateFlow

interface CreditRepository {
    val balanceFlow: StateFlow<CreditBalance>

    suspend fun getAvailableCredits(): Int
    suspend fun hasSufficientCredits(): Boolean

    suspend fun hold(operationId: String): Boolean
    suspend fun consume(operationId: String): Boolean
    suspend fun release(operationId: String): Boolean

    suspend fun getTransactionHistory(): List<CreditOperation>

    suspend fun addPurchasedCredits(amount: Int)
    suspend fun setBalance(free: Int, purchased: Int)
    suspend fun resetForTesting(free: Int, purchased: Int)
}
