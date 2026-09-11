package com.example

import com.example.data.ReadingSession
import com.example.data.computeReadingAnalytics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReadingAnalyticsTest {

    @Test
    fun testEmptySessions_returnsZeroTotalsAndSevenDayStats() {
        val now = System.currentTimeMillis()
        val analytics = computeReadingAnalytics(emptyList(), now)

        assertEquals(0L, analytics.todaySeconds)
        assertEquals(0, analytics.todaySessionsCount)
        assertEquals(0L, analytics.weekSeconds)
        assertEquals(0, analytics.weekSessionsCount)
        assertEquals(0L, analytics.dailyAverageThisWeekSeconds)
        assertEquals(7, analytics.weeklyDayStats.size)
        assertTrue(analytics.weeklyDayStats.last().isToday)
    }

    @Test
    fun testDailyAndWeeklyAggregation() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = cal.timeInMillis
        val oneDayMillis = 24L * 60L * 60L * 1000L

        val sessions = listOf(
            // Today session 1: 1800s (30m)
            ReadingSession(id = 1, durationSeconds = 1800L, startTimeMillis = now - 1800000L, endTimeMillis = now),
            // Today session 2: 900s (15m)
            ReadingSession(id = 2, durationSeconds = 900L, startTimeMillis = now - 900000L, endTimeMillis = now - 60000L),
            // Yesterday session: 1200s (20m)
            ReadingSession(id = 3, durationSeconds = 1200L, startTimeMillis = now - oneDayMillis, endTimeMillis = now - oneDayMillis + 1200000L),
            // 3 days ago session: 3600s (60m)
            ReadingSession(id = 4, durationSeconds = 3600L, startTimeMillis = now - (3 * oneDayMillis), endTimeMillis = now - (3 * oneDayMillis) + 3600000L),
            // 10 days ago (outside the 7-day week): 5000s
            ReadingSession(id = 5, durationSeconds = 5000L, startTimeMillis = now - (10 * oneDayMillis), endTimeMillis = now - (10 * oneDayMillis) + 5000000L)
        )

        val analytics = computeReadingAnalytics(sessions, now)

        // Today: 1800 + 900 = 2700s
        assertEquals(2700L, analytics.todaySeconds)
        assertEquals(2, analytics.todaySessionsCount)

        // Yesterday: 1200s
        assertEquals(1200L, analytics.yesterdaySeconds)
        assertEquals(1, analytics.yesterdaySessionsCount)

        // Weekly (last 7 days): today (2700) + yesterday (1200) + 3 days ago (3600) = 7500s
        assertEquals(7500L, analytics.weekSeconds)
        assertEquals(4, analytics.weekSessionsCount)
        assertEquals(7500L / 7L, analytics.dailyAverageThisWeekSeconds)

        // All time: 7500 + 5000 = 12500s
        assertEquals(12500L, analytics.allTimeSeconds)
        assertEquals(5, analytics.allTimeSessionsCount)

        // 7 days generated
        assertEquals(7, analytics.weeklyDayStats.size)
        val todayStat = analytics.weeklyDayStats.first { it.isToday }
        assertEquals(2700L, todayStat.totalSeconds)
        assertEquals(2, todayStat.sessionCount)
    }
}
