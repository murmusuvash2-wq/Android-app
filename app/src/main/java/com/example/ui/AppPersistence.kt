package com.example.ui

import android.content.Context
import com.example.credit.repository.CreditRepositoryProvider
import com.example.share.data.SharedProductRepositoryProvider

object AppPersistence {
    fun init(context: Context) {
        try {
            CreditRepositoryProvider.init(context)
            SharedProductRepositoryProvider.init(context)
            SessionManager.init(context)
            TiHinStyleRepository.init(context)
            TryOnManager.init(context)
            com.example.data.datasource.SupabaseAuthManager.init(context)
        } catch (_: Exception) {
            // Safe fallback for in-memory / testing environments
        }
    }
}
