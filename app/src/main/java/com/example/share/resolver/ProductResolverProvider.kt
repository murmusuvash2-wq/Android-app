package com.example.share.resolver

/**
 * Singleton provider managing the active ProductResolver instance.
 * Allows swapping custom or mock resolvers for testing.
 */
object ProductResolverProvider {
    private var instance: ProductResolver = DefaultProductResolver()

    fun get(): ProductResolver = instance

    fun setForTesting(resolver: ProductResolver) {
        instance = resolver
    }

    fun reset() {
        instance = DefaultProductResolver()
    }
}
