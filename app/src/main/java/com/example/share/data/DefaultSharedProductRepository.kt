package com.example.share.data

import com.example.data.local.SharedProductDao
import com.example.data.local.SharedProductEntity
import com.example.data.model.Product
import com.example.data.model.SharedProductRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Default implementation of SharedProductRepository.
 * Manages in-memory cache for fast, synchronous access while synchronizing with Room DB.
 */
class DefaultSharedProductRepository(
    private val dao: SharedProductDao? = null,
    private val userId: String = "usr_maya_01"
) : SharedProductRepository {

    private val cache = CopyOnWriteArrayList<SharedProductRecord>()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        loadFromDaoSync()
    }

    private fun loadFromDaoSync() {
        if (dao != null) {
            try {
                val entities = dao.getSharedProductsSync(userId)
                cache.clear()
                cache.addAll(entities.map { it.toRecord() })
            } catch (_: Exception) {
                // Fallback to in-memory cache if DB not accessible
            }
        }
    }

    override fun saveSharedProduct(
        product: Product,
        originalUrl: String,
        merchantName: String?,
        timestamp: Long
    ): SharedProductRecord {
        val canonicalId = product.id
        val resolvedMerchant = merchantName ?: product.brand

        // Check if duplicate shared product already exists
        val existingIndex = cache.indexOfFirst { it.canonicalProductId == canonicalId }
        val record = if (existingIndex >= 0) {
            val existing = cache[existingIndex]
            val updated = existing.copy(
                originalSharedUrl = originalUrl,
                merchantName = resolvedMerchant,
                productName = product.name,
                productImage = product.primaryImageUrl,
                price = product.price,
                resolvedAt = timestamp
            )
            cache.removeAt(existingIndex)
            cache.add(0, updated)
            updated
        } else {
            val newRecord = SharedProductRecord(
                id = UUID.randomUUID().toString(),
                canonicalProductId = canonicalId,
                merchantName = resolvedMerchant,
                originalSharedUrl = originalUrl,
                productName = product.name,
                productImage = product.primaryImageUrl,
                price = product.price,
                resolvedAt = timestamp
            )
            cache.add(0, newRecord)
            newRecord
        }

        // Persist to Room asynchronously
        if (dao != null) {
            ioScope.launch {
                try {
                    dao.insert(record.toEntity(userId))
                } catch (_: Exception) {}
            }
        }

        return record
    }

    override fun getSharedProducts(): List<SharedProductRecord> {
        return cache.toList()
    }

    override fun getRecentSharedProduct(): SharedProductRecord? {
        return cache.firstOrNull()
    }

    override fun getSharedRecordForProduct(productId: String): SharedProductRecord? {
        return cache.firstOrNull { it.canonicalProductId == productId }
    }

    override fun clear() {
        cache.clear()
        if (dao != null) {
            ioScope.launch {
                try {
                    dao.deleteAll(userId)
                } catch (_: Exception) {}
            }
        }
    }

    private fun SharedProductEntity.toRecord(): SharedProductRecord {
        return SharedProductRecord(
            id = id,
            canonicalProductId = canonicalProductId,
            merchantName = merchantName,
            originalSharedUrl = originalSharedUrl,
            productName = productName,
            productImage = productImage,
            price = price,
            resolvedAt = resolvedAt
        )
    }

    private fun SharedProductRecord.toEntity(uid: String): SharedProductEntity {
        return SharedProductEntity(
            id = id,
            canonicalProductId = canonicalProductId,
            merchantName = merchantName,
            originalSharedUrl = originalSharedUrl,
            productName = productName,
            productImage = productImage,
            price = price,
            resolvedAt = resolvedAt,
            userId = uid
        )
    }
}
