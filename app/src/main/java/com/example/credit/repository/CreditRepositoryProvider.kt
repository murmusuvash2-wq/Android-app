package com.example.credit.repository

import android.content.Context

object CreditRepositoryProvider {
    @Volatile
    private var instance: CreditRepository? = null

    private val initLock = Any()

    fun init(context: Context) {
        if (instance == null) {
            synchronized(initLock) {
                if (instance == null) {
                    val appContext = context.applicationContext ?: context
                    instance = LocalCreditRepository(appContext)
                }
            }
        }
    }

    fun setForTesting(repo: CreditRepository) {
        instance = repo
    }

    fun get(): CreditRepository {
        return instance ?: throw IllegalStateException("CreditRepository not initialized")
    }
}
