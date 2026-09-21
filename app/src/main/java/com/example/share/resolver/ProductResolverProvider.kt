package com.example.share.resolver

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
