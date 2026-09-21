package com.example.data.repository

/**
 * Singleton provider managing the active ProductRepository instance.
 * Allows swapping in custom or mock implementations for testing.
 */
object ProductRepositoryProvider {
    private var instance: ProductRepository = DefaultProductRepository()

    fun get(): ProductRepository = instance

    fun setForTesting(repository: ProductRepository) {
        instance = repository
    }

    fun reset() {
        instance = DefaultProductRepository()
    }
}
