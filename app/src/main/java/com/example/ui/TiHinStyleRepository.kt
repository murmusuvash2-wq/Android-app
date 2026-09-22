package com.example.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.example.data.local.AppDatabase
import com.example.data.local.FavouriteProductEntity
import com.example.data.local.TrackedProductEntity
import com.example.data.local.SavedTryOnResultEntity
import com.example.data.model.Product
import com.example.data.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SavedLook(
    val id: String,
    val productId: String,
    val productName: String,
    val brand: String,
    val price: Double,
    val resultImageUri: String,
    val isFavourite: Boolean = false,
    val isPriceTracked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

object TiHinStyleRepository {
    val favouriteProductIds = mutableStateListOf<String>()
    val priceTrackedProductIds = mutableStateListOf<String>()
    val savedLooks = mutableStateListOf<SavedLook>()
    private val favouriteMetadata = mutableStateMapOf<String, FavouriteMetadata>()

    private data class FavouriteMetadata(
        val title: String,
        val brand: String,
        val price: Double,
        val imageUrl: String
    )

    private var database: AppDatabase? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun init(context: Context) {
        if (database != null) return
        val db = AppDatabase.getInstance(context)
        database = db

        // Load Favourites
        scope.launch {
            db.favouriteProductDao().getFavouriteProductIds("usr_maya_01").collectLatest { ids ->
                favouriteProductIds.clear()
                favouriteProductIds.addAll(ids)
            }
        }

        // Load Price Tracking
        scope.launch {
            db.trackedProductDao().getTrackedProducts("usr_maya_01").collectLatest { products ->
                priceTrackedProductIds.clear()
                priceTrackedProductIds.addAll(products.map { it.id })
            }
        }
        
        // Load Saved Looks (Try-On Results)
        scope.launch {
            db.savedTryOnResultDao().getSavedResults("usr_maya_01").collectLatest { entities ->
                savedLooks.clear()
                savedLooks.addAll(entities.map { entity ->
                    SavedLook(
                        id = entity.id,
                        productId = entity.productId,
                        productName = entity.productName,
                        brand = entity.productBrand,
                        price = entity.productPrice,
                        resultImageUri = entity.resultImage,
                        isFavourite = isFavourite(entity.productId),
                        isPriceTracked = isPriceTracked(entity.productId),
                        timestamp = entity.orderIndex
                    )
                })
            }
        }
    }

    fun isFavourite(productId: String): Boolean {
        return favouriteProductIds.contains(productId)
    }

    fun isPriceTracked(productId: String): Boolean {
        return priceTrackedProductIds.contains(productId)
    }

    fun setFavourite(
        productId: String,
        isFav: Boolean,
        productName: String = "",
        merchant: String = "",
        price: Double = 0.0,
        imageUrl: String = ""
    ) {
        if (isFav) {
            if (!favouriteProductIds.contains(productId)) {
                // Optimistic UI update
                if (!favouriteProductIds.contains(productId)) {
                    favouriteProductIds.add(0, productId)
                }
                
                scope.launch(Dispatchers.IO) {
                    database?.favouriteProductDao()?.insert(
                        FavouriteProductEntity(productId = productId)
                    )
                }

                if (productName.isNotBlank() || merchant.isNotBlank() || imageUrl.isNotBlank()) {
                    favouriteMetadata[productId] = FavouriteMetadata(
                        title = productName,
                        brand = merchant,
                        price = price,
                        imageUrl = imageUrl
                    )
                }
            }
        } else {
            favouriteProductIds.remove(productId)
            favouriteMetadata.remove(productId)
            
            scope.launch(Dispatchers.IO) {
                database?.favouriteProductDao()?.delete(productId, "usr_maya_01")
            }
        }
    }

    fun toggleFavourite(
        productId: String,
        productName: String = "",
        merchant: String = "",
        price: Double = 0.0,
        imageUrl: String = ""
    ): Boolean {
        val currentlyFav = isFavourite(productId)
        val newState = !currentlyFav
        setFavourite(productId, newState, productName, merchant, price, imageUrl)
        return newState
    }

    fun togglePriceTracking(productId: String): Boolean {
        return if (priceTrackedProductIds.contains(productId)) {
            priceTrackedProductIds.remove(productId)
            scope.launch(Dispatchers.IO) {
                database?.trackedProductDao()?.deleteById(productId, "usr_maya_01")
            }
            false
        } else {
            priceTrackedProductIds.add(productId)
            
            // Get metadata if possible
            val look = getProductLookup(productId)
            scope.launch(Dispatchers.IO) {
                database?.trackedProductDao()?.insert(
                    TrackedProductEntity(
                        id = productId,
                        productName = look.productName,
                        productImage = look.resultImageUri,
                        merchant = look.brand,
                        currentPrice = look.price
                    )
                )
            }
            true
        }
    }

    fun addSavedLook(look: SavedLook) {
        savedLooks.removeAll { it.id == look.id }
        savedLooks.add(0, look)
        
        scope.launch(Dispatchers.IO) {
            database?.savedTryOnResultDao()?.insert(
                SavedTryOnResultEntity(
                    id = look.id,
                    productId = look.productId,
                    productName = look.productName,
                    productBrand = look.brand,
                    productPrice = look.price,
                    resultImage = look.resultImageUri,
                    userPhoto = TryOnManager.selectedUserPhotoUri?.toString() ?: "",
                    outfitImage = look.resultImageUri, // Simplified
                    createdAt = look.timestamp.toString(),
                    isFavourite = isFavourite(look.productId),
                    orderIndex = look.timestamp,
                    cardHeight = 400 // Default height
                )
            )
        }
    }

    fun deleteSavedLook(id: String) {
        savedLooks.removeAll { it.id == id }
        scope.launch(Dispatchers.IO) {
            database?.savedTryOnResultDao()?.deleteById(id)
        }
    }

    fun getProductLookup(productId: String): SavedLook {
        // 1. Existing virtual try-on look takes precedence
        val existing = savedLooks.firstOrNull { it.productId == productId }
        // 2. Catalog product from ProductRepository source of truth
        val product = ProductRepository.get().getProductById(productId)
        // 3. Fallback to captured favourite metadata
        val meta = favouriteMetadata[productId]

        val name = existing?.productName ?: product?.name ?: meta?.title ?: "Style Piece"
        val brand = existing?.brand ?: product?.brand ?: meta?.brand ?: "TiHin"
        val price = existing?.price ?: product?.price ?: meta?.price ?: 0.0
        val imageUri = existing?.resultImageUri
            ?: product?.primaryImageUrl
            ?: product?.imageUrl
            ?: meta?.imageUrl
            ?: ""
        val id = existing?.id ?: "look_$productId"

        return SavedLook(
            id = id,
            productId = productId,
            productName = name,
            brand = brand,
            price = price,
            resultImageUri = imageUri,
            isFavourite = isFavourite(productId),
            isPriceTracked = isPriceTracked(productId),
            timestamp = existing?.timestamp ?: System.currentTimeMillis()
        )
    }

    fun resetForTesting(context: Context? = null) {
        favouriteProductIds.clear()
        priceTrackedProductIds.clear()
        savedLooks.clear()
        favouriteMetadata.clear()
        
        context?.let { ctx ->
            scope.launch(Dispatchers.IO) {
                val db = AppDatabase.getInstance(ctx)
                db.favouriteProductDao().deleteAll("usr_maya_01")
                db.trackedProductDao().deleteAll("usr_maya_01")
                db.savedTryOnResultDao().deleteAll("usr_maya_01")
            }
        }
    }
}
