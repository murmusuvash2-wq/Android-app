package com.example.data.datasource

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApi {

    @POST("auth/v1/token")
    suspend fun signInWithPassword(
        @Header("apikey") apiKey: String,
        @Query("grant_type") grantType: String = "password",
        @Body request: SupabaseAuthRequest
    ): SupabaseAuthResponseDto
}
