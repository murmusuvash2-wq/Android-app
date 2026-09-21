package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a saved Try-On result.
 * Persists user photo reference, outfit reference, result image, and product metadata.
 */
@Entity(tableName = "saved_try_on_results")
data class SavedTryOnResultEntity(
    @PrimaryKey val id: String,
    val userPhoto: String,
    val outfitImage: String,
    val resultImage: String,
    val createdAt: String,
    val isFavourite: Boolean,
    val productId: String,
    val productName: String,
    val productBrand: String,
    val productPrice: Double,
    val cardHeight: Int,
    val orderIndex: Long = System.currentTimeMillis(),
    val userId: String = "usr_maya_01"
)

/**
 * Entity representing a user's Try-On reference photo.
 * Supports up to 5 photos with a default selection flag and display ordering.
 */
@Entity(tableName = "try_on_photos")
data class TryOnPhotoEntity(
    @PrimaryKey val id: String,
    val uri: String,
    val isDefault: Boolean = false,
    val orderIndex: Int = 0,
    val userId: String = "usr_maya_01",
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Entity representing a price-tracked product.
 * Preserves product details, tracking state, and product-specific target price across restarts.
 */
@Entity(tableName = "tracked_products")
data class TrackedProductEntity(
    @PrimaryKey val id: String,
    val productImage: String,
    val productName: String,
    val merchant: String,
    val currentPrice: Double,
    val targetPrice: Double? = null,
    val productUrl: String = "",
    val trackedAt: String = "Just now",
    val isTrackingEnabled: Boolean = true,
    val userId: String = "usr_maya_01",
    val orderIndex: Long = System.currentTimeMillis()
)

/**
 * Entity representing a user's favourited product.
 */
@Entity(tableName = "favourite_products", primaryKeys = ["productId", "userId"])
data class FavouriteProductEntity(
    val productId: String,
    val userId: String = "usr_maya_01",
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Entity representing a resolved shared product record.
 * Persists share history and references canonical Product identity in ProductRepository.
 */
@Entity(tableName = "shared_products")
data class SharedProductEntity(
    @PrimaryKey val id: String,
    val canonicalProductId: String,
    val merchantName: String?,
    val originalSharedUrl: String,
    val productName: String?,
    val productImage: String?,
    val price: Double?,
    val resolvedAt: Long = System.currentTimeMillis(),
    val userId: String = "usr_maya_01"
)
