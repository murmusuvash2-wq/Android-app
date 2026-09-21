package com.example.data.datasource

import com.example.data.model.Product
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseProductDto(
    val id: String,
    val name: String,
    val brand: String,
    @Json(name = "product_images") val productImages: List<String> = emptyList(),
    val price: Double = 0.0,
    @Json(name = "original_price") val originalPrice: Double? = null,
    val rating: Double? = null,
    @Json(name = "review_count") val reviewCount: Int? = null,
    val description: String? = null,
    @Json(name = "style_tip") val styleTip: String? = null,
    val sizes: List<String>? = null,
    val colors: List<String>? = null,
    val material: String? = null,
    @Json(name = "merchant_id") val merchantId: String? = null,
    @Json(name = "merchant_url") val merchantUrl: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "love_count") val loveCount: Int? = null,
    @Json(name = "try_on_count") val tryOnCount: Int? = null,
    @Json(name = "sales_count") val salesCount: Int? = null,
    @Json(name = "trending_score") val trendingScore: Double? = null,
    @Json(name = "is_active") val isActive: Boolean? = true
) {
    fun toDomainProduct(): Product {
        return Product(
            id = id,
            name = name,
            brand = brand,
            productImages = productImages,
            price = price,
            originalPrice = originalPrice,
            rating = rating,
            reviewCount = reviewCount,
            description = description,
            styleTip = styleTip,
            cardHeight = 240,
            sizes = sizes,
            colors = colors,
            material = material,
            merchantId = merchantId,
            merchantUrl = merchantUrl,
            createdAt = null,
            loveCount = loveCount,
            tryOnCount = tryOnCount,
            salesCount = salesCount,
            trendingScore = trendingScore,
            isFavourite = false
        )
    }
}
