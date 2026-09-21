package com.example.data.datasource

import android.content.Context
import android.net.Uri

sealed class TryOnResult {
    data class Success(
        val jobId: String,
        val resultId: String?,
        val resultStoragePath: String?,
        val signedResultUrl: String?,
        val watermarkApplied: Boolean,
        val productId: String,
        val requestId: String
    ) : TryOnResult()

    data class Failure(
        val errorCode: String,
        val errorMessage: String,
        val requestId: String
    ) : TryOnResult()

    data class Error(
        val exception: Throwable,
        val requestId: String
    ) : TryOnResult()
}

interface TryOnService {
    suspend fun executeTryOn(
        context: Context,
        productId: String,
        userPhotoUri: Uri,
        requestId: String
    ): TryOnResult
}
