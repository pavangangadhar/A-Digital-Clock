package com.example

import com.example.data.ReadingBreakState
import com.example.data.computeRealTimeBreakSuggestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingBreakTest {

    @Test
    fun testRealTimeBreakSuggestion_under25Minutes_suggests5Min() {
        // 10 minutes elapsed
        val suggestion10 = computeRealTimeBreakSuggestion(10 * 60L)
        assertEquals(5, suggestion10.durationMinutes)
        assertEquals("5-Min Quick Eye Rest", suggestion10.title)
        assertEquals("Suggested: 5m", suggestion10.badgeLabel)

        // 24 minutes elapsed
        val suggestion24 = computeRealTimeBreakSuggestion(24 * 60L)
        assertEquals(5, suggestion24.durationMinutes)
    }

    @Test
    fun testRealTimeBreakSuggestion_25to49Minutes_suggests10Min() {
        // 25 minutes elapsed
        val suggestion25 = computeRealTimeBreakSuggestion(25 * 60L)
        assertEquals(10, suggestion25.durationMinutes)
        assertEquals("10-Min Stretch & Hydrate", suggestion25.title)
        assertEquals("Suggested: 10m", suggestion25.badgeLabel)

        // 45 minutes elapsed
        val suggestion45 = computeRealTimeBreakSuggestion(45 * 60L)
        assertEquals(10, suggestion45.durationMinutes)
    }

    @Test
    fun testRealTimeBreakSuggestion_50PlusMinutes_suggests15Min() {
        // 50 minutes elapsed
        val suggestion50 = computeRealTimeBreakSuggestion(50 * 60L)
        assertEquals(15, suggestion50.durationMinutes)
        assertEquals("15-Min Deep Recharge", suggestion50.title)
        assertEquals("Suggested: 15m", suggestion50.badgeLabel)

        // 90 minutes elapsed
        val suggestion90 = computeRealTimeBreakSuggestion(90 * 60L)
        assertEquals(15, suggestion90.durationMinutes)
    }

    @Test
    fun testReadingBreakState_progressAndFormatting() {
        val state = ReadingBreakState(
            isBreakActive = true,
            breakTotalSeconds = 300,
            breakRemainingSeconds = 150,
            selectedDurationMinutes = 5
        )

        assertEquals("02:30", state.formattedRemaining)
        assertEquals(0.5f, state.progress, 0.001f)
        assertFalse(state.isCompleted)

        val completedState = state.copy(breakRemainingSeconds = 0, isCompleted = true)
        assertEquals("00:00", completedState.formattedRemaining)
        assertEquals(1.0f, completedState.progress, 0.001f)
        assertTrue(completedState.isCompleted)
    }
}
