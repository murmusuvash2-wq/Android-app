package com.example.share.data

import com.example.data.model.Product
import com.example.data.model.SharedProductRecord

/**
 * Repository foundation managing saved shared product records.
 * Acts as a separate user activity store and reference to canonical ProductRepository models.
 */
interface SharedProductRepository {
    /**
     * Persists a resolved shared product record for later retrieval.
     */
    fun saveSharedProduct(
        product: Product,
        originalUrl: String,
        merchantName: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): SharedProductRecord

    /**
     * Returns all saved shared product records, ordered with most recent first.
     */
    fun getSharedProducts(): List<SharedProductRecord>

    /**
     * Returns the most recently resolved shared product record, if any.
     */
    fun getRecentSharedProduct(): SharedProductRecord?

    /**
     * Returns the record for a specific canonical product ID, if it was shared.
     */
    fun getSharedRecordForProduct(productId: String): SharedProductRecord?

    /**
     * Clears shared product history (used during testing or cache reset).
     */
    fun clear()

    companion object {
        fun get(): SharedProductRepository = SharedProductRepositoryProvider.get()
    }
}
