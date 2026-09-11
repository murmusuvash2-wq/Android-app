package com.example.data.model

import java.util.UUID

/**
 * Representation of a resolved product shared from an external shopping app.
 * Acts as a user-specific activity and bookmark record; references canonical
 * Product ID in ProductRepository without duplicating catalog ownership.
 */
data class SharedProductRecord(
    val id: String = UUID.randomUUID().toString(),
    val canonicalProductId: String,
    val merchantName: String?,
    val originalSharedUrl: String,
    val productName: String? = null,
    val productImage: String? = null,
    val price: Double? = null,
    val resolvedAt: Long = System.currentTimeMillis()
)
