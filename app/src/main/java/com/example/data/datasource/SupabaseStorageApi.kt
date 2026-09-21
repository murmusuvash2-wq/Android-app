package com.example.data.datasource

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApi {

    @POST("storage/v1/object/{bucket}/{path}")
    suspend fun uploadObject(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authHeader: String,
        @Header("x-upsert") upsert: String = "true",
        @Path("bucket") bucket: String,
        @Path(value = "path", encoded = true) path: String,
        @Body fileBody: RequestBody
    ): Response<ResponseBody>
}
