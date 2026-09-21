package com.example.share.model

/**
 * Domain input model capturing incoming product share data.
 * Pure Kotlin representation free of Android Intent or UI dependencies.
 */
data class SharedProductInput(
    val sourceUrl: String,
    val sourcePackageName: String? = null,
    val sharedText: String? = null
)
