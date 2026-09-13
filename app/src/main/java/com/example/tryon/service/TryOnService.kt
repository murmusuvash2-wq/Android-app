package com.example.tryon.service

import com.example.tryon.model.TryOnRequest
import com.example.tryon.model.TryOnResult

/**
 * Backend-ready contract for virtual try-on generation.
 *
 * Design constraints:
 * - Asynchronous and coroutine cancellation-aware.
 * - Does not depend on Jetpack Compose UI.
 * - Does not manipulate Android navigation.
 * - Does not own or mutate credits (managed strictly by SessionManager).
 * - Does not persist user favourites or price tracking (managed by TiHinStyleRepository).
 */
interface TryOnService {

    /**
     * Generates a virtual try-on result asynchronously for the given [request].
     *
     * @param request the validated input parameters including product and photo references.
     * @return [TryOnResult] representing the completed generation output.
     * @throws com.example.tryon.model.TryOnException on structured domain failures.
     * @throws kotlinx.coroutines.CancellationException if cancelled.
     */
    suspend fun generate(request: TryOnRequest): TryOnResult

    /**
     * Signals cancellation for an in-flight request matching [requestId].
     * Implementations can use this for idempotent background cleanup.
     */
    fun cancel(requestId: String) {}
}
