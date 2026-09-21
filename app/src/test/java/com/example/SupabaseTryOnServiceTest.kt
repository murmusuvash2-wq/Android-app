package com.example

import com.example.data.datasource.GenerateTryOnRequestDto
import com.example.data.datasource.GenerateTryOnResponseDto
import com.example.data.datasource.SupabaseConfig
import com.example.data.datasource.TryOnResult
import com.example.data.datasource.TryOnService
import com.example.data.datasource.TryOnServiceProvider
import com.example.ui.TryOnManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class SupabaseTryOnServiceTest {

    @Before
    fun setup() {
        TryOnManager.resetForTesting()
    }

    @Test
    fun `test GenerateTryOnRequestDto serialization`() {
        val requestId = "req_${UUID.randomUUID()}"
        val dto = GenerateTryOnRequestDto(
            productId = "prod-123",
            userPhotoPath = "user-uuid/photo.jpg",
            requestId = requestId
        )

        val adapter = SupabaseConfig.moshi.adapter(GenerateTryOnRequestDto::class.java)
        val json = adapter.toJson(dto)

        assertTrue(json.contains("\"productId\":\"prod-123\""))
        assertTrue(json.contains("\"userPhotoPath\":\"user-uuid/photo.jpg\""))
        assertTrue(json.contains("\"requestId\":\"$requestId\""))
    }

    @Test
    fun `test GenerateTryOnResponseDto deserialization success`() {
        val json = """
            {
                "jobId": "job-abc-123",
                "resultId": "res-xyz-789",
                "status": "COMPLETED",
                "resultStoragePath": "tryon-results/user/job.jpg",
                "signedResultUrl": "https://example.com/signed.jpg",
                "watermarkApplied": true,
                "productId": "1",
                "requestId": "req_12345"
            }
        """.trimIndent()

        val adapter = SupabaseConfig.moshi.adapter(GenerateTryOnResponseDto::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("job-abc-123", response?.jobId)
        assertEquals("COMPLETED", response?.status)
        assertEquals("https://example.com/signed.jpg", response?.signedResultUrl)
        assertTrue(response?.watermarkApplied == true)
        assertEquals("1", response?.productId)
        assertEquals("req_12345", response?.requestId)
        assertNull(response?.error)
    }

    @Test
    fun `test GenerateTryOnResponseDto deserialization error`() {
        val json = """
            {
                "error": "product_unavailable",
                "message": "Product is not available for virtual try-on"
            }
        """.trimIndent()

        val adapter = SupabaseConfig.moshi.adapter(GenerateTryOnResponseDto::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("product_unavailable", response?.error)
        assertEquals("Product is not available for virtual try-on", response?.message)
        assertNull(response?.jobId)
    }

    @Test
    fun `test TryOnManager updates and clears state correctly`() {
        TryOnManager.selectedProductId = "prod-456"
        TryOnManager.selectedProductName = "Silk Blazer"
        TryOnManager.selectedProductBrand = "MANGO"
        TryOnManager.selectedProductPrice = 5990.0
        TryOnManager.isProcessing = true
        TryOnManager.lastRequestId = "req-999"

        assertEquals("prod-456", TryOnManager.selectedProductId)
        assertEquals("Silk Blazer", TryOnManager.selectedProductName)
        assertEquals(5990.0, TryOnManager.selectedProductPrice, 0.01)
        assertTrue(TryOnManager.isProcessing)
        assertEquals("req-999", TryOnManager.lastRequestId)

        TryOnManager.clear()

        assertNull(TryOnManager.selectedProductId)
        assertNull(TryOnManager.selectedProductName)
        assertEquals(0.0, TryOnManager.selectedProductPrice, 0.01)
        assertFalse(TryOnManager.isProcessing)
        assertNull(TryOnManager.lastRequestId)
    }

    @Test
    fun `test TryOnServiceProvider injects and resets custom implementation`() = runBlocking {
        val mockService = object : TryOnService {
            override suspend fun executeTryOn(
                context: android.content.Context,
                productId: String,
                userPhotoUri: android.net.Uri,
                requestId: String
            ): TryOnResult {
                return TryOnResult.Success(
                    jobId = "mock-job-1",
                    resultId = "mock-res-1",
                    resultStoragePath = "tryon-results/user/look.jpg",
                    signedResultUrl = "https://example.com/look.jpg",
                    watermarkApplied = true,
                    productId = productId,
                    requestId = requestId
                )
            }
        }

        TryOnServiceProvider.setForTesting(mockService)
        assertSame(mockService, TryOnServiceProvider.get())

        TryOnServiceProvider.setForTesting(null)
        assertNotNull(TryOnServiceProvider.get())
    }

    @Test
    fun `test Phase 3 Result Delivery signed URL received and mapped to TryOnResult Success`() {
        val jobId = "job_${UUID.randomUUID()}"
        val resultId = "res_${UUID.randomUUID()}"
        val reqId = "req_${UUID.randomUUID()}"
        val signedUrl = "https://example-supabase.co/storage/v1/object/sign/tryon-results/user-123/$jobId.jpg?token=secret123"

        val json = """
            {
                "jobId": "$jobId",
                "resultId": "$resultId",
                "status": "COMPLETED",
                "resultStoragePath": "user-123/$jobId.jpg",
                "signedResultUrl": "$signedUrl",
                "watermarkApplied": true,
                "productId": "prod-456",
                "requestId": "$reqId",
                "completedAt": "2026-09-20T00:00:00.000Z"
            }
        """.trimIndent()

        val adapter = SupabaseConfig.moshi.adapter(GenerateTryOnResponseDto::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("COMPLETED", response?.status)
        assertEquals(signedUrl, response?.signedResultUrl)
        assertEquals("user-123/$jobId.jpg", response?.resultStoragePath)
        assertEquals(reqId, response?.requestId)
        assertEquals(jobId, response?.jobId)
        assertEquals(resultId, response?.resultId)

        val resultSuccess = TryOnResult.Success(
            jobId = response!!.jobId!!,
            resultId = response.resultId,
            resultStoragePath = response.resultStoragePath,
            signedResultUrl = response.signedResultUrl,
            watermarkApplied = response.watermarkApplied ?: false,
            productId = response.productId ?: "prod-456",
            requestId = response.requestId ?: reqId
        )

        assertEquals(signedUrl, resultSuccess.signedResultUrl)
        assertEquals("user-123/$jobId.jpg", resultSuccess.resultStoragePath)
        assertTrue(resultSuccess.watermarkApplied)
    }

    @Test
    fun `test GenerateTryOnRequestDto supports cancel action for job cancellation`() {
        val reqId = "req_cancel_123"
        val dto = GenerateTryOnRequestDto(
            action = "cancel",
            requestId = reqId,
            jobId = "job_789"
        )

        val adapter = SupabaseConfig.moshi.adapter(GenerateTryOnRequestDto::class.java)
        val json = adapter.toJson(dto)

        assertTrue(json.contains("\"action\":\"cancel\""))
        assertTrue(json.contains("\"requestId\":\"$reqId\""))
        assertTrue(json.contains("\"jobId\":\"job_789\""))
    }
}
