package com.example.data.datasource

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateTryOnRequestDto(
    @Json(name = "productId") val productId: String? = null,
    @Json(name = "userPhotoPath") val userPhotoPath: String? = null,
    @Json(name = "requestId") val requestId: String? = null,
    @Json(name = "action") val action: String? = null,
    @Json(name = "jobId") val jobId: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateTryOnResponseDto(
    @Json(name = "jobId") val jobId: String? = null,
    @Json(name = "resultId") val resultId: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "resultStoragePath") val resultStoragePath: String? = null,
    @Json(name = "signedResultUrl") val signedResultUrl: String? = null,
    @Json(name = "watermarkApplied") val watermarkApplied: Boolean? = null,
    @Json(name = "productId") val productId: String? = null,
    @Json(name = "requestId") val requestId: String? = null,
    @Json(name = "completedAt") val completedAt: String? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "message") val message: String? = null
)
