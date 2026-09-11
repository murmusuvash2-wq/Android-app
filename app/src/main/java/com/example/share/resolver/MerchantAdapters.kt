package com.example.share.resolver

import android.net.Uri
import com.example.share.model.SharedProductInput

/**
 * Shared utility for extracting product IDs or keywords from URLs and shared text.
 */
internal object ResolverUrlUtils {
    private val KNOWN_PRODUCT_IDS = setOf(
        "1", "2", "3", "4", "5", "6",
        "p1", "p2", "p3", "p4", "p5", "p6",
        "home_trending_1", "home_trending_2", "home_trending_3", "home_trending_4",
        "trending_1", "trending_2", "trending_3", "trending_4"
    )

    fun extractExplicitId(url: String): String? {
        val uri = try { Uri.parse(url) } catch (_: Exception) { return null }

        // 1. Check query parameters (e.g. ?id=1, ?pid=3, ?productId=5, ?sku=2)
        val queryKeys = listOf("id", "pid", "productId", "sku", "item_id", "p", "tihin_id")
        for (key in queryKeys) {
            val value = uri.getQueryParameter(key)
            if (!value.isNullOrBlank() && KNOWN_PRODUCT_IDS.contains(value)) {
                return value
            }
        }

        // 2. Check path segments (e.g. /product/1, /p/2, /dp/3, /dresses/4)
        val segments = uri.pathSegments ?: emptyList()
        for (segment in segments) {
            if (KNOWN_PRODUCT_IDS.contains(segment)) {
                return segment
            }
        }

        return null
    }

    fun matchKeywords(input: SharedProductInput): String? {
        val fullText = "${input.sourceUrl} ${input.sharedText.orEmpty()}".lowercase()
        return when {
            fullText.contains("cashmere") || fullText.contains("trench") -> "1"
            fullText.contains("overcoat") || fullText.contains("wool") -> "2"
            fullText.contains("linen") || fullText.contains("blazer") -> "3"
            fullText.contains("oxford") || fullText.contains("trouser") -> "4"
            fullText.contains("emerald") || fullText.contains("satin") || fullText.contains("maxi") -> "5"
            fullText.contains("loungewear") || fullText.contains("waffle") || fullText.contains("co-ord") -> "6"
            fullText.contains("day dress") -> "home_trending_1"
            fullText.contains("knit polo") || fullText.contains("chinos") -> "home_trending_2"
            fullText.contains("denim") || fullText.contains("tee") -> "home_trending_3"
            fullText.contains("blush") -> "home_trending_4"
            else -> null
        }
    }

    fun hostMatches(url: String, domainPattern: String): Boolean {
        val host = try { Uri.parse(url).host?.lowercase() } catch (_: Exception) { null }
        return host != null && (host == domainPattern || host.endsWith(".$domainPattern"))
    }
}

class ZaraResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "ZARA"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "zara.com") ||
                input.sourcePackageName == "com.inditex.zara" ||
                input.sourceUrl.contains("zara", ignoreCase = true)
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "1" // Canonical flagship Zara Cashmere Trench
    }
}

class HnMResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "H&M"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "hm.com") ||
                input.sourcePackageName == "com.hm.goe" ||
                input.sourceUrl.contains("hm.com", ignoreCase = true)
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "2" // Canonical H&M Tailored Wool Overcoat
    }
}

class MangoResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "MANGO"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "mango.com") ||
                input.sourceUrl.contains("mango.com", ignoreCase = true)
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "3" // Canonical Mango Minimalist Linen Blazer
    }
}

class UrbanicResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "URBANIC"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "urbanic.com") ||
                input.sourceUrl.contains("urbanic.com", ignoreCase = true)
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "5" // Canonical Urbanic Emerald Satin Maxi Dress
    }
}

class AsosResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "ASOS"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "asos.com") ||
                input.sourceUrl.contains("asos.com", ignoreCase = true)
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "6" // Canonical ASOS Pastel Co-ord Loungewear
    }
}

class MyntraResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "MYNTRA"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "myntra.com") ||
                input.sourcePackageName == "com.myntra.android" ||
                input.sourceUrl.contains("myntra.com", ignoreCase = true)
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "1"
    }
}

class AmazonResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "AMAZON"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "amazon.in") ||
                ResolverUrlUtils.hostMatches(input.sourceUrl, "amazon.com") ||
                ResolverUrlUtils.hostMatches(input.sourceUrl, "amzn.to") ||
                input.sourcePackageName == "com.amazon.mShop.android.shopping"
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "2"
    }
}

class FlipkartResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "FLIPKART"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "flipkart.com") ||
                ResolverUrlUtils.hostMatches(input.sourceUrl, "fkrt.it") ||
                input.sourcePackageName == "com.flipkart.android"
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "3"
    }
}

class AjioResolverAdapter : MerchantResolverAdapter {
    override val merchantName: String = "AJIO"

    override fun canHandle(input: SharedProductInput): Boolean {
        return ResolverUrlUtils.hostMatches(input.sourceUrl, "ajio.com") ||
                input.sourcePackageName == "com.ril.ajio"
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
            ?: "5"
    }
}

class GenericSupportedUrlAdapter : MerchantResolverAdapter {
    override val merchantName: String = "TIHIN_STYLE"

    override fun canHandle(input: SharedProductInput): Boolean {
        val url = input.sourceUrl
        return ResolverUrlUtils.hostMatches(url, "tihin.style") ||
                ResolverUrlUtils.hostMatches(url, "onme.style") ||
                ResolverUrlUtils.hostMatches(url, "example.com") ||
                ResolverUrlUtils.extractExplicitId(url) != null
    }

    override fun resolveCanonicalProductId(input: SharedProductInput): String? {
        return ResolverUrlUtils.extractExplicitId(input.sourceUrl)
            ?: ResolverUrlUtils.matchKeywords(input)
    }
}
