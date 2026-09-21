package com.example.credit.model

enum class CreditSource {
    FREE, PURCHASED
}

enum class TransactionState {
    HELD, CONSUMED, RELEASED
}

data class CreditOperation(
    val operationId: String,
    val amount: Int = 1,
    val state: TransactionState,
    val source: CreditSource?,
    val timestamp: Long = System.currentTimeMillis()
)

data class CreditBalance(
    val freeCredits: Int,
    val purchasedCredits: Int
) {
    val total: Int get() = freeCredits + purchasedCredits
}
