package com.example.ui

import android.content.Context
import com.example.share.data.SharedProductRepositoryProvider

/**
 * Centralized coordinator for initializing local persistence subsystems.
 * Ensures consistent startup order and hydration across Profile, Session, Photos, Styles, and Shared Products.
 */
object AppPersistence {

    fun init(context: Context) {
        val appContext = context.applicationContext

        // 1. Profile
        UserProfileRepository.init(appContext)

        // 2. Session & Credits
        SessionManager.init(appContext)

        // 3. User Try-On Photos
        UserPhotosRepository.init(appContext, UserProfileRepository.profile.userId)

        // 4. Style, Favourites, Price Tracking & Saved Results
        OnMeStyleRepository.init(appContext, UserProfileRepository.profile.userId)

        // 5. Shared Products History
        SharedProductRepositoryProvider.init(appContext, UserProfileRepository.profile.userId)

        // 6. Pre-fill Try-On screen with default user photo if available and not set
        if (!SessionManager.isGuest && TryOnManager.selectedUserPhotoUri.isBlank()) {
            UserPhotosRepository.defaultPhotoUri?.let { uri ->
                TryOnManager.selectedUserPhotoUri = uri
            }
        }
    }

    /**
     * Synchronous initialization for testing to ensure full hydration before assertions.
     */
    fun initSync(context: Context) {
        val appContext = context.applicationContext
        UserProfileRepository.init(appContext)
        SessionManager.init(appContext)
        UserPhotosRepository.initSync(appContext, UserProfileRepository.profile.userId)
        OnMeStyleRepository.initSync(appContext, UserProfileRepository.profile.userId)
        SharedProductRepositoryProvider.init(appContext, UserProfileRepository.profile.userId)
        if (!SessionManager.isGuest && TryOnManager.selectedUserPhotoUri.isBlank()) {
            UserPhotosRepository.defaultPhotoUri?.let { uri ->
                TryOnManager.selectedUserPhotoUri = uri
            }
        }
    }
}
