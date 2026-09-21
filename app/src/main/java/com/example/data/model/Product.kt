package com.example.data.model

/**
 * Canonical product representation for TiHin.
 *
 * Serves as the single domain model for catalog items across Home, Discover, Looks,
 * and Try-On flows. All catalog data is treated as immutable and read-only from
 * the UI perspective.
 */
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val productImages: List<String>,
    val price: Double,
    val originalPrice: Double? = null,
    val rating: Double? = null,
    val reviewCount: Int? = null,
    val description: String? = null,
    val styleTip: String? = null,
    val cardHeight: Int = 240,
    val sizes: List<String>? = null,
    val colors: List<String>? = null,
    val material: String? = null,
    val merchantId: String? = null,
    val merchantUrl: String? = null,
    val createdAt: Long? = null,
    val loveCount: Int? = null,
    val tryOnCount: Int? = null,
    val salesCount: Int? = null,
    val trendingScore: Double? = null,
    val isFavourite: Boolean = false
) {
    /**
     * Primary product image for display across cards, carousels, and try-on previews.
     */
    val primaryImageUrl: String
        get() = productImages.firstOrNull().orEmpty()

    /**
     * Backward-compatibility accessor for primary image URL.
     */
    val imageUrl: String
        get() = primaryImageUrl

    /**
     * Backward-compatibility accessor for merchant/brand label.
     */
    val merchant: String
        get() = brand

    /**
     * Secondary constructor preserving full backward compatibility with legacy DiscoverProduct calls.
     */
    constructor(
        id: String,
        name: String,
        merchant: String,
        productImages: List<String>,
        description: String? = null,
        styleTip: String? = null,
        rating: Double? = null,
        reviewCount: Int? = null,
        price: Double,
        cardHeight: Int = 240,
        isFavourite: Boolean = false
    ) : this(
        id = id,
        name = name,
        brand = merchant,
        productImages = productImages,
        price = price,
        originalPrice = null,
        rating = rating,
        reviewCount = reviewCount,
        description = description,
        styleTip = styleTip,
        cardHeight = cardHeight,
        sizes = null,
        colors = null,
        material = null,
        merchantId = null,
        merchantUrl = null,
        createdAt = null,
        loveCount = null,
        tryOnCount = null,
        salesCount = null,
        trendingScore = null,
        isFavourite = isFavourite
    )
}
