package com.example

import com.example.ui.SessionManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CreditSafetyUnitTest {

    @Before
    fun setUp() {
        SessionManager.isGuest = false
        SessionManager.credits = 2
        SessionManager.releaseHeldCredit()
    }

    @Test
    fun testAuthenticatedUser_initialCreditGrantIsTwo() {
        SessionManager.isGuest = false
        SessionManager.credits = 2
        SessionManager.releaseHeldCredit()

        assertEquals(2, SessionManager.credits)
        assertEquals(2, SessionManager.availableCredits)
        assertTrue(SessionManager.hasSufficientCredits)
    }

    @Test
    fun testGuestUser_initialCreditGrantIsZero() {
        SessionManager.isGuest = true
        SessionManager.credits = 0
        SessionManager.releaseHeldCredit()

        assertEquals(0, SessionManager.credits)
        assertEquals(0, SessionManager.availableCredits)
        assertTrue("Guests have unlimited client-side tries without credit hold", SessionManager.hasSufficientCredits)
    }

    @Test
    fun testHoldCredit_successWhenCreditsAvailable() {
        SessionManager.credits = 5
        val held = SessionManager.holdCredit()

        assertTrue(held)
        assertTrue(SessionManager.hasActiveHold)
        assertEquals(5, SessionManager.credits) // Total credits not decremented yet
        assertEquals(4, SessionManager.availableCredits) // Available credits reflect hold
    }

    @Test
    fun testHoldCredit_idempotentOnRepeatedTaps() {
        SessionManager.credits = 3
        val firstHold = SessionManager.holdCredit()
        val secondHold = SessionManager.holdCredit() // double tap

        assertTrue(firstHold)
        assertTrue(secondHold)
        assertTrue(SessionManager.hasActiveHold)
        assertEquals(3, SessionManager.credits)
        assertEquals(2, SessionManager.availableCredits) // exactly 1 held, not 2
    }

    @Test
    fun testHoldCredit_failsWhenZeroCredits() {
        SessionManager.credits = 0
        val held = SessionManager.holdCredit()

        assertFalse(held)
        assertFalse(SessionManager.hasActiveHold)
        assertFalse(SessionManager.hasSufficientCredits)
        assertEquals(0, SessionManager.availableCredits)
    }

    @Test
    fun testConsumeHeldCredit_deductsExactlyOne() {
        SessionManager.credits = 10
        SessionManager.holdCredit()

        val consumed = SessionManager.consumeHeldCredit()

        assertTrue(consumed)
        assertFalse(SessionManager.hasActiveHold)
        assertEquals(9, SessionManager.credits)
        assertEquals(9, SessionManager.availableCredits)
    }

    @Test
    fun testConsumeHeldCredit_cannotDoubleDeduct() {
        SessionManager.credits = 10
        SessionManager.holdCredit()

        val firstConsume = SessionManager.consumeHeldCredit()
        val secondConsume = SessionManager.consumeHeldCredit()

        assertTrue(firstConsume)
        assertFalse(secondConsume) // Already consumed, cannot double deduct
        assertFalse(SessionManager.hasActiveHold)
        assertEquals(9, SessionManager.credits)
    }

    @Test
    fun testReleaseHeldCredit_restoresFullBalanceOnCancelOrFailure() {
        SessionManager.credits = 7
        SessionManager.holdCredit()
        assertEquals(6, SessionManager.availableCredits)

        SessionManager.releaseHeldCredit()

        assertFalse(SessionManager.hasActiveHold)
        assertEquals(7, SessionManager.credits)
        assertEquals(7, SessionManager.availableCredits)
    }

    @Test
    fun testGuestUser_neverHoldsOrDeductsCredits() {
        SessionManager.isGuest = true
        SessionManager.credits = 0

        assertTrue(SessionManager.hasSufficientCredits)
        assertTrue(SessionManager.holdCredit())
        assertFalse(SessionManager.hasActiveHold)

        assertTrue(SessionManager.consumeHeldCredit())
        assertEquals(0, SessionManager.credits)
    }

    @Test
    fun testNoNegativeBalance() {
        SessionManager.credits = 1
        SessionManager.holdCredit()
        SessionManager.consumeHeldCredit()

        assertEquals(0, SessionManager.credits)

        // Attempting to consume without hold does nothing
        SessionManager.consumeHeldCredit()
        assertEquals(0, SessionManager.credits)
    }
}
