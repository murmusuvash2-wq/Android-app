package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey
    val operationId: String,
    val amount: Int,
    val state: String, // "HELD", "CONSUMED", "RELEASED"
    val source: String, // "FREE", "PURCHASED"
    val timestamp: Long
)
