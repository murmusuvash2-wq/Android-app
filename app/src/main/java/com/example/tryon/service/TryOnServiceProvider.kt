package com.example.tryon.service

/**
 * Service locator / provider for [TryOnService].
 * Allows clean substitution for unit testing and future backend migration
 * without modifying UI or navigation layers.
 */
object TryOnServiceProvider {

    @Volatile
    private var instance: TryOnService? = null

    /**
     * Gets the current [TryOnService] instance, defaulting to [LocalMockTryOnService].
     */
    fun get(): TryOnService {
        return instance ?: synchronized(this) {
            instance ?: LocalMockTryOnService().also { instance = it }
        }
    }

    /**
     * Sets a custom service instance (e.g., in unit or Robolectric tests).
     */
    fun setForTesting(service: TryOnService?) {
        instance = service
    }

    /**
     * Resets the service provider to its default state.
     */
    fun reset() {
        instance = null
    }
}
