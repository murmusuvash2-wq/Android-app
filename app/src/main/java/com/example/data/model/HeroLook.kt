package com.example.data.model

/**
 * Represents a curated "See the Magic" hero showcase item.
 * References the canonical Product via [productId].
 */
data class HeroLook(
    val id: String,
    val productId: String,
    val hangerImage: String? = null,
    val wornImage: String? = null
)
