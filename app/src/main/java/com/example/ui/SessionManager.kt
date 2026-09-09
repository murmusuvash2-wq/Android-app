package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SessionManager {
    var isGuest by mutableStateOf(false)
    var credits by mutableIntStateOf(2)

    // Active credit hold for an in-flight Try-On attempt
    var hasActiveHold by mutableStateOf(false)
        private set

    /**
     * Effective credits available for new operations, factoring in any active hold.
     */
    val availableCredits: Int
        get() = if (!isGuest && hasActiveHold) (credits - 1).coerceAtLeast(0) else credits

    /**
     * Checks if user has sufficient credits to initiate a Try-On (guests always allowed).
     */
    val hasSufficientCredits: Boolean
        get() = isGuest || availableCredits >= 1

    /**
     * Reserves 1 credit for an in-flight Try-On attempt.
     * Idempotent: repeated calls while a hold is already active return true without holding extra credits.
     * Returns true if hold is active or successfully placed, false if insufficient credits.
     */
    fun holdCredit(): Boolean {
        if (isGuest) return true
        if (hasActiveHold) return true
        if (credits >= 1) {
            hasActiveHold = true
            return true
        }
        return false
    }

    /**
     * Permanently consumes the held credit upon successful result generation.
     * Exactly 1 credit is deducted and the hold is cleared.
     * Safe against negative balances and duplicate consumption.
     */
    fun consumeHeldCredit(): Boolean {
        if (isGuest) return true
        if (hasActiveHold) {
            credits = (credits - 1).coerceAtLeast(0)
            hasActiveHold = false
            return true
        }
        return false
    }

    /**
     * Releases any active credit hold without deducting from balance.
     * Called on cancellation, failure, or navigating away.
     */
    fun releaseHeldCredit() {
        hasActiveHold = false
    }
}
