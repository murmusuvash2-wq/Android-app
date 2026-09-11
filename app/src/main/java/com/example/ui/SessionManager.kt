package com.example.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.credit.repository.CreditRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object SessionManager {
    private const val PREFS_NAME = "tihin_session_prefs"
    private const val KEY_IS_GUEST = "is_guest"
    private const val KEY_PRICE_DROP_NOTIFS = "price_drop_notifs"
    private const val KEY_HAS_INITIALIZED = "has_initialized"
    private const val KEY_HAS_RECEIVED_INITIAL_GRANT = "has_received_initial_grant"

    private var appContext: Context? = null
    
    private var _isGuest by mutableStateOf(false)
    var isGuest: Boolean
        get() = _isGuest
        set(value) {
            setSessionMode(value)
        }

    private var _freeCredits by mutableIntStateOf(0)
    val freeCredits: Int get() = if (_isGuest) 0 else _freeCredits

    private var _purchasedCredits by mutableIntStateOf(0)
    val purchasedCredits: Int get() = if (_isGuest) 0 else _purchasedCredits

    private var _priceDropNotificationsEnabled by mutableStateOf(true)
    var priceDropNotificationsEnabled: Boolean
        get() = _priceDropNotificationsEnabled
        set(value) {
            _priceDropNotificationsEnabled = value
            saveState()
        }

    val credits: Int get() = if (_isGuest) 0 else (_freeCredits + _purchasedCredits)

    private var _availableCredits by mutableIntStateOf(0)
    val availableCredits: Int get() = if (_isGuest) 0 else _availableCredits
    
    val hasActiveHold: Boolean get() = false
    
    val hasSufficientCredits: Boolean
        get() = isGuest || _availableCredits >= 1

    fun refreshAvailableCredits() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _availableCredits = CreditRepositoryProvider.get().getAvailableCredits()
            } catch (e: Exception) {}
        }
    }

    private var isInitialized = false
    private val initMutex = Any()

    fun init(context: Context) {
        synchronized(initMutex) {
            if (isInitialized) return
            isInitialized = true
        }
        
        appContext = context.applicationContext
        
        CreditRepositoryProvider.init(context)
        
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hasInitialized = prefs.getBoolean(KEY_HAS_INITIALIZED, false)
            if (!hasInitialized) {
                _isGuest = false
                _priceDropNotificationsEnabled = true
                saveState()
            } else {
                _isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
                _priceDropNotificationsEnabled = prefs.getBoolean(KEY_PRICE_DROP_NOTIFS, true)
            }
            
            // Check if authenticated user needs initial grant
            if (!_isGuest && !prefs.getBoolean(KEY_HAS_RECEIVED_INITIAL_GRANT, false)) {
                CoroutineScope(Dispatchers.IO).launch {
                    val repo = CreditRepositoryProvider.get()
                    if (repo.balanceFlow.value.total == 0) {
                        repo.setBalance(2, 0)
                    }
                }
                prefs.edit().putBoolean(KEY_HAS_RECEIVED_INITIAL_GRANT, true).apply()
            }
        } catch (e: Exception) {
            _isGuest = false
            _priceDropNotificationsEnabled = true
        }

        CoroutineScope(Dispatchers.Default).launch {
            val repo = CreditRepositoryProvider.get()
            repo.balanceFlow.collect { balance ->
                _freeCredits = balance.freeCredits
                _purchasedCredits = balance.purchasedCredits
                refreshAvailableCredits()
            }
        }
    }

    fun addPurchasedCredits(amount: Int, context: Context? = null) {
        if (amount <= 0) return
        CoroutineScope(Dispatchers.IO).launch {
            CreditRepositoryProvider.get().addPurchasedCredits(amount)
            refreshAvailableCredits()
        }
    }

    fun setPriceDropNotifications(enabled: Boolean, context: Context? = null) {
        priceDropNotificationsEnabled = enabled
    }

    fun setSessionMode(asGuest: Boolean, context: Context? = null) {
        _isGuest = asGuest
        val targetContext = context?.applicationContext ?: appContext
        
        if (!asGuest && targetContext != null) {
            val prefs = targetContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_HAS_RECEIVED_INITIAL_GRANT, false)) {
                CoroutineScope(Dispatchers.IO).launch {
                    val repo = CreditRepositoryProvider.get()
                    if (repo.balanceFlow.value.total == 0) {
                        repo.setBalance(2, 0)
                    }
                }
                prefs.edit().putBoolean(KEY_HAS_RECEIVED_INITIAL_GRANT, true).apply()
            }
        }
        
        refreshAvailableCredits()
        saveState(context)
    }

    fun logout(context: Context? = null) {
        setSessionMode(asGuest = true, context = context)
    }

    fun releaseHeldCredit() {}

    private fun saveState(context: Context? = null) {
        val targetContext = context?.applicationContext ?: appContext ?: return
        try {
            val prefs = targetContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_HAS_INITIALIZED, true)
                .putBoolean(KEY_IS_GUEST, _isGuest)
                .putBoolean(KEY_PRICE_DROP_NOTIFS, _priceDropNotificationsEnabled)
                .apply()
        } catch (e: Exception) {
        }
    }

    fun resetForTesting(
        initialGuest: Boolean = false,
        initialFree: Int = 2,
        initialPurchased: Int = 0,
        initialNotifications: Boolean = true,
        context: Context? = null
    ) {
        synchronized(initMutex) {
            isInitialized = false
        }
        _isGuest = initialGuest
        _freeCredits = initialFree
        _purchasedCredits = initialPurchased
        runBlocking {
            CreditRepositoryProvider.get().resetForTesting(initialFree, initialPurchased)
            _availableCredits = CreditRepositoryProvider.get().getAvailableCredits()
        }
        
        if (appContext == null) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val repo = CreditRepositoryProvider.get()
                    repo.balanceFlow.collect { balance ->
                        _freeCredits = balance.freeCredits
                        _purchasedCredits = balance.purchasedCredits
                        refreshAvailableCredits()
                    }
                } catch(e: Exception) {}
            }
        }
        
        _priceDropNotificationsEnabled = initialNotifications
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().clear()
                     .putBoolean(KEY_HAS_RECEIVED_INITIAL_GRANT, true)
                     .apply()
            } catch (e: Exception) {
            }
        }
    }
}
