package com.example.data.datasource

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SupabaseProductApi {

    @GET("rest/v1/products")
    suspend fun getActiveProducts(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Query("is_active") isActive: String = "eq.true",
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 1000,
        @Query("offset") offset: Int = 0
    ): List<SupabaseProductDto>
}
