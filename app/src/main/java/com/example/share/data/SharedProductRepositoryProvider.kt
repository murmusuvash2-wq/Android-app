package com.example.share.data

import android.content.Context
import com.example.data.local.AppDatabase

/**
 * Singleton provider for SharedProductRepository.
 */
object SharedProductRepositoryProvider {
    private var instance: SharedProductRepository = DefaultSharedProductRepository()

    fun get(): SharedProductRepository = instance

    fun init(context: Context, userId: String = "usr_maya_01") {
        try {
            val db = AppDatabase.getInstance(context)
            instance = DefaultSharedProductRepository(db.sharedProductDao(), userId)
        } catch (_: Exception) {
            instance = DefaultSharedProductRepository()
        }
    }

    fun setForTesting(repository: SharedProductRepository) {
        instance = repository
    }

    fun reset() {
        instance = DefaultSharedProductRepository()
    }
}
