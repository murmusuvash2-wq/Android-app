package com.example.data.datasource

object TryOnServiceProvider {

    private var instance: TryOnService? = null

    fun get(): TryOnService {
        return instance ?: SupabaseTryOnService().also { instance = it }
    }

    fun setForTesting(service: TryOnService?) {
        instance = service
    }
}
