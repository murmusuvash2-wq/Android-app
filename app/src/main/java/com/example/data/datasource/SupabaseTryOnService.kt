package com.example.data.datasource

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class SupabaseTryOnService(
    private val authManager: SupabaseAuthManager = SupabaseAuthManager,
    private val config: SupabaseConfig = SupabaseConfig
) : TryOnService {

    companion object {
        private const val TAG = "SupabaseTryOnService"
        private const val BUCKET_NAME = "tryon-photos"
    }

    override suspend fun executeTryOn(
        context: Context,
        productId: String,
        userPhotoUri: Uri,
        requestId: String
    ): TryOnResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting Try-On flow - ProductId: $productId, RequestId: $requestId")

            // 1. Authenticate / get session
            val sessionResult = authManager.ensureAuthenticatedSession()
            if (sessionResult.isFailure) {
                val errorMsg = sessionResult.exceptionOrNull()?.message ?: "Failed to acquire Supabase session"
                Log.e(TAG, "Auth failed: $errorMsg")
                return@withContext TryOnResult.Failure("auth_error", errorMsg, requestId)
            }

            val (userId, token) = sessionResult.getOrThrow()
            Log.d(TAG, "Using User ID: $userId")

            // 2. Read user photo bytes
            val photoBytes = try {
                context.contentResolver.openInputStream(userPhotoUri)?.use { it.readBytes() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read image bytes: ${e.message}", e)
                null
            }

            if (photoBytes == null || photoBytes.isEmpty()) {
                Log.e(TAG, "Photo bytes empty for URI: $userPhotoUri")
                return@withContext TryOnResult.Failure("invalid_photo", "Unable to read selected photo data", requestId)
            }

            // 3. User-scoped storage path: <userId>/<unique-photo-file>
            val uniqueFileName = "user_photo_${UUID.randomUUID()}.jpg"
            val userPhotoPath = "$userId/$uniqueFileName"

            Log.d(TAG, "Uploading photo to $BUCKET_NAME/$userPhotoPath (${photoBytes.size} bytes)")

            // 4. Upload photo to Supabase Storage
            val requestBody = photoBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val uploadResponse = config.storageApi.uploadObject(
                apiKey = config.anonKey,
                authHeader = "Bearer $token",
                upsert = "true",
                bucket = BUCKET_NAME,
                path = userPhotoPath,
                fileBody = requestBody
            )

            if (!uploadResponse.isSuccessful) {
                val errorBody = uploadResponse.errorBody()?.string() ?: ""
                Log.e(TAG, "Storage upload failed: HTTP ${uploadResponse.code()} - $errorBody")
                return@withContext TryOnResult.Failure(
                    errorCode = "upload_failed",
                    errorMessage = "Storage upload failed (${uploadResponse.code()}): $errorBody",
                    requestId = requestId
                )
            }

            Log.d(TAG, "Storage upload successful: $userPhotoPath")

            // 5. Invoke generate-tryon Edge Function
            val requestDto = GenerateTryOnRequestDto(
                productId = productId,
                userPhotoPath = userPhotoPath,
                requestId = requestId
            )

            Log.d(TAG, "Invoking generate-tryon Edge Function: $requestDto")

            val functionResponse = config.functionsApi.generateTryOn(
                apiKey = config.anonKey,
                authHeader = "Bearer $token",
                request = requestDto
            )

            val rawBody = if (functionResponse.isSuccessful) {
                functionResponse.body()?.string() ?: ""
            } else {
                functionResponse.errorBody()?.string() ?: ""
            }

            Log.d(TAG, "Edge Function response HTTP ${functionResponse.code()}: $rawBody")

            val adapter = config.moshi.adapter(GenerateTryOnResponseDto::class.java)
            val responseDto = try {
                if (rawBody.isNotBlank()) adapter.fromJson(rawBody) else null
            } catch (e: Exception) {
                Log.w(TAG, "Could not parse response DTO: ${e.message}")
                null
            }

            if (functionResponse.isSuccessful && responseDto != null && responseDto.error == null) {
                Log.d(TAG, "Try-on completed successfully: JobId ${responseDto.jobId}")
                return@withContext TryOnResult.Success(
                    jobId = responseDto.jobId ?: "",
                    resultId = responseDto.resultId,
                    resultStoragePath = responseDto.resultStoragePath,
                    signedResultUrl = responseDto.signedResultUrl,
                    watermarkApplied = responseDto.watermarkApplied ?: false,
                    productId = responseDto.productId ?: productId,
                    requestId = responseDto.requestId ?: requestId
                )
            } else {
                val errorCode = responseDto?.error ?: "edge_function_error_${functionResponse.code()}"
                val errorMessage = responseDto?.message ?: rawBody.ifBlank { "Edge Function returned HTTP ${functionResponse.code()}" }
                Log.e(TAG, "Try-on Edge Function error: $errorCode - $errorMessage")
                return@withContext TryOnResult.Failure(
                    errorCode = errorCode,
                    errorMessage = errorMessage,
                    requestId = requestId
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in executeTryOn: ${e.message}", e)
            return@withContext TryOnResult.Error(e, requestId)
        }
    }
}
