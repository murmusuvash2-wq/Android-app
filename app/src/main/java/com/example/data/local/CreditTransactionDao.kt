package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CreditTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: CreditTransactionEntity)

    @Query("SELECT * FROM credit_transactions WHERE operationId = :operationId")
    suspend fun getTransaction(operationId: String): CreditTransactionEntity?

    @Query("SELECT * FROM credit_transactions WHERE state = 'HELD'")
    suspend fun getHeldTransactions(): List<CreditTransactionEntity>

    @Query("SELECT * FROM credit_transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactions(): List<CreditTransactionEntity>

    @Query("DELETE FROM credit_transactions")
    suspend fun clear()
}
