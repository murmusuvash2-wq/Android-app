package com.example.data.datasource

import com.example.data.model.Product

/**
 * In-memory mock data source providing the TiHin prototype product catalog.
 * Isolates mock data from UI layers, ensuring clean transitions to future network data sources.
 */
object MockProductDataSource : ProductDataSource {

    /**
     * Canonical catalog products displayed in Discover and Peek & Reveal.
     * Product IDs "1" through "6" are strictly preserved to maintain backward compatibility.
     */
    val CATALOG_PRODUCTS: List<Product> = listOf(
        Product(
            id = "1",
            name = "Oversized Cashmere Trench",
            brand = "ZARA",
            productImages = listOf(
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=800&q=80"
            ),
            description = "Spun from ultra-soft Mongolian cashmere with an elegant draped storm flap and tonal horn buttons.",
            styleTip = "Layer open over high-waisted wool trousers and pointed ankle boots for a structured silhouette.",
            rating = 4.8,
            reviewCount = 124,
            price = 12999.0,
            cardHeight = 260
        ),
        Product(
            id = "2",
            name = "Tailored Wool Overcoat",
            brand = "H&M",
            productImages = listOf(
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?auto=format&fit=crop&w=800&q=80"
            ),
            description = "Structured double-breasted silhouette cut from premium recycled wool blend.",
            styleTip = "Pair with an oatmeal rollneck and leather loafers for timeless winter sophistication.",
            rating = 4.6,
            reviewCount = 89,
            price = 8999.0,
            cardHeight = 220
        ),
        Product(
            id = "3",
            name = "Minimalist Linen Blazer",
            brand = "MANGO",
            productImages = listOf(
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=800&q=80"
            ),
            description = "Breathable pure European linen tailored with relaxed notch lapels and natural corozo buttons.",
            styleTip = "Wear cuffs slightly pushed up with matching wide-leg trousers and gold hoops.",
            rating = 4.9,
            reviewCount = 210,
            price = 6590.0,
            cardHeight = 280
        ),
        Product(
            id = "4",
            name = "Structured Oxford & Trousers",
            brand = "ZARA",
            productImages = listOf(
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80"
            ),
            description = "Classic crisp cotton Oxford pairing seamlessly with straight-leg pleats.",
            styleTip = "Half-tuck into belted charcoal trousers for an effortlessly sharp weekday profile.",
            rating = 4.5,
            reviewCount = 67,
            price = 4590.0,
            cardHeight = 210
        ),
        Product(
            id = "5",
            name = "Emerald Satin Maxi Dress",
            brand = "URBANIC",
            productImages = listOf(
                "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=800&q=80"
            ),
            description = "Fluid bias-cut lustrous satin dress with an open cowl back and subtle train.",
            styleTip = "Style minimally with delicate barely-there metallic sandals and a sleek low chignon.",
            rating = 4.7,
            reviewCount = 148,
            price = 3790.0,
            cardHeight = 250
        ),
        Product(
            id = "6",
            name = "Pastel Co-ord Loungewear",
            brand = "ASOS",
            productImages = listOf(
                "https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80"
            ),
            description = "Soft waffle-knit matching ensemble designed for effortless elevated lounging.",
            styleTip = "Ideal for off-duty days; finish the look with chunky slides and a slouchy tote.",
            rating = null,
            reviewCount = null,
            price = 3290.0,
            cardHeight = 230
        )
    )

    /**
     * Curated trending looks featured on the Home screen.
     */
    val HOME_TRENDING_PRODUCTS: List<Product> = listOf(
        Product(
            id = "home_trending_1",
            name = "Linen Day Dress",
            brand = "URBANIC",
            productImages = listOf("https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?auto=format&fit=crop&w=400&q=80"),
            price = 2499.0,
            cardHeight = 240
        ),
        Product(
            id = "home_trending_2",
            name = "Knit Polo & Chinos",
            brand = "ZARA",
            productImages = listOf("https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?auto=format&fit=crop&w=400&q=80"),
            price = 3999.0,
            cardHeight = 240
        ),
        Product(
            id = "home_trending_3",
            name = "Everyday Denim & Tee",
            brand = "LEVI'S",
            productImages = listOf("https://images.unsplash.com/photo-1558769132-cb1aea458c5e?auto=format&fit=crop&w=400&q=80"),
            price = 2899.0,
            cardHeight = 240
        ),
        Product(
            id = "home_trending_4",
            name = "Blush Co-ord Set",
            brand = "H&M",
            productImages = listOf("https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=400&q=80"),
            price = 3299.0,
            cardHeight = 240
        )
    )

    /**
     * Deterministic ID mapping supporting legacy aliases used across Looks fixtures and test suites.
     * (e.g. "p1" maps to "1", "p2" maps to "2").
     */
    private val ID_ALIASES = mapOf(
        "p1" to "1",
        "p2" to "2",
        "p3" to "3",
        "p4" to "4",
        "p5" to "5",
        "p6" to "6",
        "trending_1" to "home_trending_1",
        "trending_2" to "home_trending_2",
        "trending_3" to "home_trending_3",
        "trending_4" to "home_trending_4",
        "t1" to "home_trending_1",
        "t2" to "home_trending_2",
        "t3" to "home_trending_3",
        "t4" to "home_trending_4"
    )

    override fun getProducts(): List<Product> = CATALOG_PRODUCTS

    override fun getTrendingProducts(): List<Product> = HOME_TRENDING_PRODUCTS

    override fun getProductById(id: String): Product? {
        // 1. Direct match in catalog
        CATALOG_PRODUCTS.firstOrNull { it.id == id }?.let { return it }

        // 2. Direct match in trending
        HOME_TRENDING_PRODUCTS.firstOrNull { it.id == id }?.let { return it }

        // 3. Resolve alias if present
        val resolvedId = ID_ALIASES[id] ?: return null
        return CATALOG_PRODUCTS.firstOrNull { it.id == resolvedId }
            ?: HOME_TRENDING_PRODUCTS.firstOrNull { it.id == resolvedId }
    }
}
