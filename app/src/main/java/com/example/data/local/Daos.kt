package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTryOnResultDao {
    @Query("SELECT * FROM saved_try_on_results WHERE userId = :userId ORDER BY orderIndex DESC")
    fun getSavedResults(userId: String): Flow<List<SavedTryOnResultEntity>>

    @Query("SELECT * FROM saved_try_on_results WHERE userId = :userId ORDER BY orderIndex DESC")
    fun getSavedResultsSync(userId: String): List<SavedTryOnResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: SavedTryOnResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(results: List<SavedTryOnResultEntity>)

    @Query("DELETE FROM saved_try_on_results WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM saved_try_on_results WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}

@Dao
interface TryOnPhotoDao {
    @Query("SELECT * FROM try_on_photos WHERE userId = :userId ORDER BY orderIndex ASC, addedAt ASC")
    fun getPhotos(userId: String): Flow<List<TryOnPhotoEntity>>

    @Query("SELECT * FROM try_on_photos WHERE userId = :userId ORDER BY orderIndex ASC, addedAt ASC")
    fun getPhotosSync(userId: String): List<TryOnPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: TryOnPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(photo: TryOnPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<TryOnPhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSync(photos: List<TryOnPhotoEntity>)

    @Update
    suspend fun update(photo: TryOnPhotoEntity)

    @Query("DELETE FROM try_on_photos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM try_on_photos WHERE userId = :userId")
    suspend fun deleteAll(userId: String)

    @Query("DELETE FROM try_on_photos WHERE userId = :userId")
    fun deleteAllSync(userId: String)

    @Query("UPDATE try_on_photos SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefault(userId: String)

    @Query("UPDATE try_on_photos SET isDefault = 0 WHERE userId = :userId")
    fun clearDefaultSync(userId: String)

    @Query("UPDATE try_on_photos SET isDefault = 1 WHERE id = :id AND userId = :userId")
    suspend fun setDefault(id: String, userId: String)
}

@Dao
interface TrackedProductDao {
    @Query("SELECT * FROM tracked_products WHERE userId = :userId AND isTrackingEnabled = 1 ORDER BY orderIndex DESC")
    fun getTrackedProducts(userId: String): Flow<List<TrackedProductEntity>>

    @Query("SELECT * FROM tracked_products WHERE userId = :userId AND isTrackingEnabled = 1 ORDER BY orderIndex DESC")
    fun getTrackedProductsSync(userId: String): List<TrackedProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: TrackedProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<TrackedProductEntity>)

    @Query("UPDATE tracked_products SET targetPrice = :targetPrice WHERE id = :id AND userId = :userId")
    suspend fun updateTargetPrice(id: String, targetPrice: Double?, userId: String)

    @Query("DELETE FROM tracked_products WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: String, userId: String)

    @Query("DELETE FROM tracked_products WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}

@Dao
interface FavouriteProductDao {
    @Query("SELECT productId FROM favourite_products WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavouriteProductIds(userId: String): Flow<List<String>>

    @Query("SELECT productId FROM favourite_products WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavouriteProductIdsSync(userId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favourite: FavouriteProductEntity)

    @Query("DELETE FROM favourite_products WHERE productId = :productId AND userId = :userId")
    suspend fun delete(productId: String, userId: String)

    @Query("DELETE FROM favourite_products WHERE userId = :userId")
    suspend fun deleteAll(userId: String)
}

@Dao
interface SharedProductDao {
    @Query("SELECT * FROM shared_products WHERE userId = :userId ORDER BY resolvedAt DESC")
    fun getSharedProducts(userId: String): Flow<List<SharedProductEntity>>

    @Query("SELECT * FROM shared_products WHERE userId = :userId ORDER BY resolvedAt DESC")
    fun getSharedProductsSync(userId: String): List<SharedProductEntity>

    @Query("SELECT * FROM shared_products WHERE canonicalProductId = :canonicalProductId AND userId = :userId LIMIT 1")
    fun getByCanonicalId(canonicalProductId: String, userId: String): SharedProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SharedProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(entity: SharedProductEntity)

    @Query("DELETE FROM shared_products WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM shared_products WHERE userId = :userId")
    suspend fun deleteAll(userId: String)

    @Query("DELETE FROM shared_products WHERE userId = :userId")
    fun deleteAllSync(userId: String)
}
