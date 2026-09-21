package com.example.data.datasource

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupabaseFunctionsApi {

    @POST("functions/v1/generate-tryon")
    suspend fun generateTryOn(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Body request: GenerateTryOnRequestDto
    ): Response<ResponseBody>
}
