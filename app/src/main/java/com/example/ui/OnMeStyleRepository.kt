package com.example.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.local.AppDatabase
import com.example.data.local.FavouriteProductEntity
import com.example.data.local.SavedTryOnResultEntity
import com.example.data.local.TrackedProductEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Shared in-memory and persistent state repository for the TiHin application.
 *
 * Provides a single source of truth for:
 * 1. Favourited items across Home, Discover, and Looks.
 * 2. Price tracked products and product-specific target prices.
 * 3. Saved try-on generated results (appearing at the top of Looks -> Recent).
 */
object OnMeStyleRepository {

    private var appContext: Context? = null
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // Set of product IDs currently marked as favourite
    var favouriteProductIds by mutableStateOf<Set<String>>(emptySet())
        private set

    // Set of product IDs currently tracked for price drops
    var trackedProductIds by mutableStateOf<Set<String>>(setOf("p1", "p3"))
        private set

    // Product-specific target prices (e.g., "p1" -> 11499.0)
    var targetPrices by mutableStateOf<Map<String, Double>>(mapOf("p1" to 11499.0, "p3" to 5990.0))
        private set

    // Saved TryOn results. New results are prepended to appear before fixtures/mocks.
    var savedResults by mutableStateOf<List<TryOnResult>>(emptyList())
        private set

    // Dynamic catalog of tracked products for display in Looks -> Price Tracking
    var customTrackedProducts by mutableStateOf<List<TrackedProduct>>(emptyList())
        private set

    /**
     * Initializes repository state from persistent Room database off the main thread.
     */
    fun init(context: Context, userId: String = UserProfileRepository.profile.userId) {
        appContext = context.applicationContext
        ioScope.launch {
            initInternal(context, userId)
        }
    }

    suspend fun initSuspend(context: Context, userId: String = UserProfileRepository.profile.userId) {
        appContext = context.applicationContext
        withContext(Dispatchers.IO) {
            initInternal(context, userId)
        }
    }

    fun initSync(context: Context, userId: String = UserProfileRepository.profile.userId) {
        appContext = context.applicationContext
        runBlocking(Dispatchers.IO) {
            initInternal(context, userId)
        }
    }

    private fun initInternal(context: Context, userId: String) {
        try {
            val db = AppDatabase.getInstance(context)

            // 1. Load Favourites
            val storedFavs = db.favouriteProductDao().getFavouriteProductIdsSync(userId)
            if (storedFavs.isNotEmpty()) {
                favouriteProductIds = storedFavs.toSet()
            }

            // 2. Load Tracked Products and Target Prices
            val storedTracked = db.trackedProductDao().getTrackedProductsSync(userId)
            if (storedTracked.isNotEmpty()) {
                trackedProductIds = storedTracked.map { it.id }.toSet()
                val targetMap = mutableMapOf<String, Double>()
                val trackedList = mutableListOf<TrackedProduct>()

                storedTracked.forEach { entity ->
                    entity.targetPrice?.let { tp -> targetMap[entity.id] = tp }
                    trackedList.add(
                        TrackedProduct(
                            id = entity.id,
                            productImage = entity.productImage,
                            productName = entity.productName,
                            merchant = entity.merchant,
                            currentPrice = entity.currentPrice,
                            targetPrice = entity.targetPrice,
                            productUrl = entity.productUrl,
                            trackedAt = entity.trackedAt
                        )
                    )
                }
                targetPrices = targetMap
                customTrackedProducts = trackedList
            } else {
                // Seed default tracked items ("p1" and "p3")
                seedDefaultTrackedProducts(db, userId)
            }

            // 3. Load Saved Try-On Results
            val storedResults = db.savedTryOnResultDao().getSavedResultsSync(userId)
            if (storedResults.isNotEmpty()) {
                savedResults = storedResults.map { entity ->
                    TryOnResult(
                        id = entity.id,
                        userPhoto = entity.userPhoto,
                        outfitImage = entity.outfitImage,
                        resultImage = entity.resultImage,
                        createdAt = entity.createdAt,
                        isFavourite = entity.isFavourite,
                        productId = entity.productId,
                        productName = entity.productName,
                        productBrand = entity.productBrand,
                        productPrice = entity.productPrice,
                        cardHeight = entity.cardHeight
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully to default in-memory state
        }
    }

    private fun seedDefaultTrackedProducts(db: AppDatabase, userId: String) {
        val initialEntities = listOf(
            TrackedProductEntity(
                id = "p1",
                productImage = "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80",
                productName = "Oversized Cashmere Trench",
                merchant = "ZARA",
                currentPrice = 12999.0,
                targetPrice = 11499.0,
                productUrl = "",
                trackedAt = "1w ago",
                userId = userId
            ),
            TrackedProductEntity(
                id = "p3",
                productImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                productName = "Minimalist Linen Blazer",
                merchant = "MANGO",
                currentPrice = 6590.0,
                targetPrice = 5990.0,
                productUrl = "",
                trackedAt = "2w ago",
                userId = userId
            )
        )
        ioScope.launch {
            try {
                db.trackedProductDao().insertAll(initialEntities)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Resets repository state to default initial conditions.
     * Useful for testing and state resets.
     */
    fun resetForTesting(
        initialFavourites: Set<String> = emptySet(),
        initialTracked: Set<String> = setOf("p1", "p3"),
        initialSavedResults: List<TryOnResult> = emptyList(),
        initialCustomTracked: List<TrackedProduct> = emptyList(),
        context: Context? = null
    ) {
        favouriteProductIds = initialFavourites
        trackedProductIds = initialTracked
        savedResults = initialSavedResults
        customTrackedProducts = initialCustomTracked
        targetPrices = mapOf("p1" to 11499.0, "p3" to 5990.0)

        context?.let { ctx ->
            runCatching {
                runBlocking(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(ctx)
                    val userId = UserProfileRepository.profile.userId
                    db.savedTryOnResultDao().deleteAll(userId)
                    db.favouriteProductDao().deleteAll(userId)
                    db.trackedProductDao().deleteAll(userId)
                }
            }
        }
    }

    private fun getLegacyAlias(id: String): String? = when (id) {
        "p1" -> "1"
        "1" -> "p1"
        "p2" -> "2"
        "2" -> "p2"
        "p3" -> "3"
        "3" -> "p3"
        "p4" -> "4"
        "4" -> "p4"
        "p5" -> "5"
        "5" -> "p5"
        "p6" -> "6"
        "6" -> "p6"
        "trending_1" -> "home_trending_1"
        "home_trending_1" -> "trending_1"
        "trending_2" -> "home_trending_2"
        "home_trending_2" -> "trending_2"
        "trending_3" -> "home_trending_3"
        "home_trending_3" -> "trending_3"
        "trending_4" -> "home_trending_4"
        "home_trending_4" -> "trending_4"
        else -> null
    }

    /**
     * Checks whether a given product ID is in favourites.
     */
    fun isFavourite(productId: String): Boolean {
        if (favouriteProductIds.contains(productId)) return true
        val alias = getLegacyAlias(productId)
        return alias != null && favouriteProductIds.contains(alias)
    }

    /**
     * Checks whether a given product ID is currently tracked for price drops.
     */
    fun isPriceTracked(productId: String): Boolean {
        if (trackedProductIds.contains(productId)) return true
        val alias = getLegacyAlias(productId)
        return alias != null && trackedProductIds.contains(alias)
    }

    /**
     * Retrieves the product-specific target price if set.
     */
    fun getTargetPrice(productId: String): Double? {
        targetPrices[productId]?.let { return it }
        val alias = getLegacyAlias(productId) ?: return null
        return targetPrices[alias]
    }

    /**
     * Sets or updates product-specific target price and persists to storage.
     */
    fun setTargetPrice(
        productId: String,
        targetPrice: Double,
        context: Context? = null,
        synchronous: Boolean = false
    ) {
        targetPrices = targetPrices + (productId to targetPrice)

        // Also update customTrackedProducts
        customTrackedProducts = customTrackedProducts.map {
            if (it.id == productId) it.copy(targetPrice = targetPrice) else it
        }

        val targetContext = context ?: appContext ?: return
        val task: suspend () -> Unit = {
            try {
                val db = AppDatabase.getInstance(targetContext)
                db.trackedProductDao().updateTargetPrice(
                    id = productId,
                    targetPrice = targetPrice,
                    userId = UserProfileRepository.profile.userId
                )
            } catch (e: Exception) {
                // Ignore
            }
        }
        if (synchronous) {
            runBlocking(Dispatchers.IO) { task() }
        } else {
            ioScope.launch { task() }
        }
    }

    /**
     * Toggles favourite state for a product.
     * Rule: When favouriting a product, price tracking is automatically enabled.
     * Rule: When unfavouriting a product, favourite status is removed; price tracking status is not forcibly removed.
     */
    fun toggleFavourite(
        productId: String,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null,
        context: Context? = null,
        synchronous: Boolean = false
    ): Boolean {
        val isCurrentlyFav = isFavourite(productId)
        val alias = getLegacyAlias(productId)
        if (isCurrentlyFav) {
            favouriteProductIds = if (alias != null) {
                favouriteProductIds - productId - alias
            } else {
                favouriteProductIds - productId
            }
            persistFavourite(productId, isFav = false, context = context, synchronous = synchronous)
            if (alias != null) {
                persistFavourite(alias, isFav = false, context = context, synchronous = synchronous)
            }
            return false
        } else {
            favouriteProductIds = favouriteProductIds + productId
            persistFavourite(productId, isFav = true, context = context, synchronous = synchronous)
            // Rule: Favouriting automatically enables price tracking
            setPriceTracking(
                productId = productId,
                enabled = true,
                productName = productName,
                merchant = merchant,
                price = price,
                imageUrl = imageUrl,
                context = context,
                synchronous = synchronous
            )
            return true
        }
    }

    /**
     * Explicitly sets favourite state and persists to database.
     */
    fun setFavourite(
        productId: String,
        isFav: Boolean,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null,
        context: Context? = null,
        synchronous: Boolean = false
    ) {
        val alias = getLegacyAlias(productId)
        if (isFav) {
            favouriteProductIds = favouriteProductIds + productId
            persistFavourite(productId, isFav = true, context = context, synchronous = synchronous)
            setPriceTracking(
                productId = productId,
                enabled = true,
                productName = productName,
                merchant = merchant,
                price = price,
                imageUrl = imageUrl,
                context = context,
                synchronous = synchronous
            )
        } else {
            favouriteProductIds = if (alias != null) {
                favouriteProductIds - productId - alias
            } else {
                favouriteProductIds - productId
            }
            persistFavourite(productId, isFav = false, context = context, synchronous = synchronous)
            if (alias != null) {
                persistFavourite(alias, isFav = false, context = context, synchronous = synchronous)
            }
        }
    }

    private fun persistFavourite(
        productId: String,
        isFav: Boolean,
        context: Context?,
        synchronous: Boolean = false
    ) {
        val targetContext = context ?: appContext ?: return
        val task: suspend () -> Unit = {
            try {
                val db = AppDatabase.getInstance(targetContext)
                val userId = UserProfileRepository.profile.userId
                if (isFav) {
                    db.favouriteProductDao().insert(
                        FavouriteProductEntity(productId = productId, userId = userId)
                    )
                } else {
                    db.favouriteProductDao().delete(productId = productId, userId = userId)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        if (synchronous) {
            runBlocking(Dispatchers.IO) { task() }
        } else {
            ioScope.launch { task() }
        }
    }

    /**
     * Toggles price tracking for a product.
     * Rule: Manually turning Price Tracking OFF does NOT remove the favourite.
     */
    fun togglePriceTracking(
        productId: String,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null,
        context: Context? = null,
        synchronous: Boolean = false
    ): Boolean {
        val currentlyTracked = isPriceTracked(productId)
        val newTracked = !currentlyTracked
        setPriceTracking(
            productId = productId,
            enabled = newTracked,
            productName = productName,
            merchant = merchant,
            price = price,
            imageUrl = imageUrl,
            context = context,
            synchronous = synchronous
        )
        return newTracked
    }

    /**
     * Explicitly sets price tracking state and persists to database.
     */
    fun setPriceTracking(
        productId: String,
        enabled: Boolean,
        productName: String? = null,
        merchant: String? = null,
        price: Double? = null,
        imageUrl: String? = null,
        context: Context? = null,
        synchronous: Boolean = false
    ) {
        val targetPriceVal = targetPrices[productId] ?: price?.let { it * 0.9 }

        if (enabled) {
            trackedProductIds = trackedProductIds + productId
            if (targetPriceVal != null && !targetPrices.containsKey(productId)) {
                targetPrices = targetPrices + (productId to targetPriceVal)
            }

            if (productName != null && imageUrl != null) {
                if (customTrackedProducts.none { it.id == productId }) {
                    val newTracked = TrackedProduct(
                        id = productId,
                        productImage = imageUrl,
                        productName = productName,
                        merchant = merchant ?: "",
                        currentPrice = price ?: 0.0,
                        targetPrice = targetPriceVal,
                        productUrl = "",
                        trackedAt = "Just now"
                    )
                    customTrackedProducts = listOf(newTracked) + customTrackedProducts
                }
            }
        } else {
            trackedProductIds = trackedProductIds - productId
            customTrackedProducts = customTrackedProducts.filterNot { it.id == productId }
        }

        val targetContext = context ?: appContext ?: return
        val task: suspend () -> Unit = {
            try {
                val db = AppDatabase.getInstance(targetContext)
                val userId = UserProfileRepository.profile.userId
                if (enabled) {
                    val existing = customTrackedProducts.find { it.id == productId }
                    val entity = TrackedProductEntity(
                        id = productId,
                        productImage = existing?.productImage ?: imageUrl ?: "",
                        productName = existing?.productName ?: productName ?: "Tracked Product",
                        merchant = existing?.merchant ?: merchant ?: "",
                        currentPrice = existing?.currentPrice ?: price ?: 0.0,
                        targetPrice = targetPriceVal,
                        productUrl = existing?.productUrl ?: "",
                        trackedAt = existing?.trackedAt ?: "Just now",
                        isTrackingEnabled = true,
                        userId = userId
                    )
                    db.trackedProductDao().insert(entity)
                } else {
                    db.trackedProductDao().deleteById(productId, userId)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        if (synchronous) {
            runBlocking(Dispatchers.IO) { task() }
        } else {
            ioScope.launch { task() }
        }
    }

    /**
     * Saves a generated TryOn result and persists to Room database.
     */
    fun saveResult(result: TryOnResult, context: Context? = null, synchronous: Boolean = false) {
        val filtered = savedResults.filterNot { it.id == result.id || (it.productId == result.productId && it.resultImage == result.resultImage) }
        savedResults = listOf(result) + filtered

        val targetContext = context ?: appContext ?: return
        val task: suspend () -> Unit = {
            try {
                val db = AppDatabase.getInstance(targetContext)
                val entity = SavedTryOnResultEntity(
                    id = result.id,
                    userPhoto = result.userPhoto,
                    outfitImage = result.outfitImage,
                    resultImage = result.resultImage,
                    createdAt = result.createdAt,
                    isFavourite = result.isFavourite,
                    productId = result.productId,
                    productName = result.productName,
                    productBrand = result.productBrand,
                    productPrice = result.productPrice,
                    cardHeight = result.cardHeight,
                    orderIndex = System.currentTimeMillis(),
                    userId = UserProfileRepository.profile.userId
                )
                db.savedTryOnResultDao().insert(entity)
            } catch (e: Exception) {
                // Ignore
            }
        }
        if (synchronous) {
            runBlocking(Dispatchers.IO) { task() }
        } else {
            ioScope.launch { task() }
        }
    }

    /**
     * Checks if a result has already been saved.
     */
    fun isResultSaved(productId: String, resultImage: String): Boolean {
        return savedResults.any { it.productId == productId && it.resultImage == resultImage }
    }

    /**
     * Gets all active tracked products, merging custom tracked products with baseline fixtures.
     * Items are filtered by `trackedProductIds`.
     */
    fun getActiveTrackedProducts(baselineFixtures: List<TrackedProduct>): List<TrackedProduct> {
        val allProducts = customTrackedProducts + baselineFixtures.filterNot { fixture ->
            customTrackedProducts.any { it.id == fixture.id }
        }
        return allProducts
            .filter { trackedProductIds.contains(it.id) }
            .map { product ->
                val currentTarget = targetPrices[product.id] ?: product.targetPrice
                if (currentTarget != product.targetPrice) product.copy(targetPrice = currentTarget) else product
            }
    }
}
