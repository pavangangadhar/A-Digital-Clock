package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * DayReadingStat
 *
 * Encapsulates the aggregated reading stats for a single calendar day.
 */
data class DayReadingStat(
    val dayOffset: Int, // 0 = today, 1 = 1 day ago, etc.
    val dateKey: String, // "yyyy-MM-dd"
    val dayName: String, // "Mon", "Tue", "Wed", etc.
    val displayLabel: String, // "Today", "Yesterday", or "Fri, Sep 11"
    val dateSubtitle: String, // "Sep 11"
    val totalSeconds: Long,
    val sessionCount: Int,
    val isToday: Boolean = false,
    val totalBreakSeconds: Long = 0L
)

/**
 * ReadingAnalytics
 *
 * Comprehensive analysis of daily and weekly reading time across all saved sessions.
 */
data class ReadingAnalytics(
    val todaySeconds: Long = 0L,
    val todaySessionsCount: Int = 0,
    val todayBreakSeconds: Long = 0L,
    val yesterdaySeconds: Long = 0L,
    val yesterdaySessionsCount: Int = 0,
    val yesterdayBreakSeconds: Long = 0L,
    val weekSeconds: Long = 0L,
    val weekSessionsCount: Int = 0,
    val weekBreakSeconds: Long = 0L,
    val dailyAverageThisWeekSeconds: Long = 0L,
    val weeklyDayStats: List<DayReadingStat> = emptyList(), // 7 days ending with today
    val maxDaySecondsInWeek: Long = 0L,
    val mostActiveDayLabel: String = "None",
    val allTimeSeconds: Long = 0L,
    val allTimeSessionsCount: Int = 0,
    val allTimeBreakSeconds: Long = 0L,
    val dayGroups: List<DayGroupedSessions> = emptyList()
)

/**
 * Grouped sessions for daily logs
 */
data class DayGroupedSessions(
    val dateKey: String,
    val dateLabel: String,
    val totalSeconds: Long,
    val sessionCount: Int,
    val sessions: List<ReadingSession>,
    val totalBreakSeconds: Long = 0L
)

/**
 * Pure calculation function for daily and weekly reading analytics.
 */
fun computeReadingAnalytics(
    sessions: List<ReadingSession>,
    nowMillis: Long = System.currentTimeMillis()
): ReadingAnalytics {
    if (sessions.isEmpty()) {
        val emptyDays = generateEmpty7Days(nowMillis)
        return ReadingAnalytics(weeklyDayStats = emptyDays)
    }

    val cal = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStartMillis = cal.timeInMillis
    val oneDayMillis = 24L * 60L * 60L * 1000L
    val weekStartMillis = todayStartMillis - (6L * oneDayMillis)

    val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateSubFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    // Generate 7 days (from 6 days ago up to today)
    val weeklyDayStats = (6 downTo 0).map { dayOffset ->
        val dayStart = todayStartMillis - (dayOffset * oneDayMillis)
        val dayEnd = dayStart + oneDayMillis
        val isToday = (dayOffset == 0)
        val isYesterday = (dayOffset == 1)

        val dayDate = Date(dayStart)
        val dateKey = keyFormat.format(dayDate)
        val dayName = dayNameFormat.format(dayDate)
        val dateSubtitle = dateSubFormat.format(dayDate)

        val displayLabel = when {
            isToday -> "Today"
            isYesterday -> "Yesterday"
            else -> fullDateFormat.format(dayDate)
        }

        val sessionsOnDay = sessions.filter {
            it.endTimeMillis in dayStart until dayEnd
        }
        val totalSecs = sessionsOnDay.sumOf { it.durationSeconds }
        val totalBreakSecs = sessionsOnDay.sumOf { it.breakDurationSeconds }

        DayReadingStat(
            dayOffset = dayOffset,
            dateKey = dateKey,
            dayName = dayName,
            displayLabel = displayLabel,
            dateSubtitle = dateSubtitle,
            totalSeconds = totalSecs,
            sessionCount = sessionsOnDay.size,
            isToday = isToday,
            totalBreakSeconds = totalBreakSecs
        )
    }

    val todayStat = weeklyDayStats.firstOrNull { it.isToday }
    val yesterdayStat = weeklyDayStats.firstOrNull { it.dayOffset == 1 }

    val todaySeconds = todayStat?.totalSeconds ?: 0L
    val todaySessionsCount = todayStat?.sessionCount ?: 0
    val todayBreakSeconds = todayStat?.totalBreakSeconds ?: 0L

    val yesterdaySeconds = yesterdayStat?.totalSeconds ?: 0L
    val yesterdaySessionsCount = yesterdayStat?.sessionCount ?: 0
    val yesterdayBreakSeconds = yesterdayStat?.totalBreakSeconds ?: 0L

    val weekSessions = sessions.filter { it.endTimeMillis >= weekStartMillis }
    val weekSeconds = weekSessions.sumOf { it.durationSeconds }
    val weekSessionsCount = weekSessions.size
    val weekBreakSeconds = weekSessions.sumOf { it.breakDurationSeconds }

    val dailyAverageThisWeekSeconds = weekSeconds / 7L
    val maxDaySecondsInWeek = weeklyDayStats.maxOfOrNull { it.totalSeconds } ?: 0L

    val mostActiveDay = weeklyDayStats
        .filter { it.totalSeconds > 0L }
        .maxByOrNull { it.totalSeconds }
    val mostActiveDayLabel = mostActiveDay?.let {
        "${it.displayLabel} (${it.dayName})"
    } ?: "None yet"

    val allTimeSeconds = sessions.sumOf { it.durationSeconds }
    val allTimeSessionsCount = sessions.size
    val allTimeBreakSeconds = sessions.sumOf { it.breakDurationSeconds }

    // Group all sessions by calendar day for historical day-by-day logs
    val groupedByDate = sessions
        .sortedByDescending { it.endTimeMillis }
        .groupBy { session ->
            val sessionDate = Date(session.endTimeMillis)
            keyFormat.format(sessionDate)
        }

    val dayGroups = groupedByDate.map { (dateKey, daySessions) ->
        val firstSessionDate = Date(daySessions.first().endTimeMillis)
        val sessionCal = Calendar.getInstance().apply {
            timeInMillis = daySessions.first().endTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sessionDayStart = sessionCal.timeInMillis
        val label = when (sessionDayStart) {
            todayStartMillis -> "Today • ${dateSubFormat.format(firstSessionDate)}"
            todayStartMillis - oneDayMillis -> "Yesterday • ${dateSubFormat.format(firstSessionDate)}"
            else -> fullDateFormat.format(firstSessionDate)
        }

        DayGroupedSessions(
            dateKey = dateKey,
            dateLabel = label,
            totalSeconds = daySessions.sumOf { it.durationSeconds },
            sessionCount = daySessions.size,
            sessions = daySessions,
            totalBreakSeconds = daySessions.sumOf { it.breakDurationSeconds }
        )
    }

    return ReadingAnalytics(
        todaySeconds = todaySeconds,
        todaySessionsCount = todaySessionsCount,
        todayBreakSeconds = todayBreakSeconds,
        yesterdaySeconds = yesterdaySeconds,
        yesterdaySessionsCount = yesterdaySessionsCount,
        yesterdayBreakSeconds = yesterdayBreakSeconds,
        weekSeconds = weekSeconds,
        weekSessionsCount = weekSessionsCount,
        weekBreakSeconds = weekBreakSeconds,
        dailyAverageThisWeekSeconds = dailyAverageThisWeekSeconds,
        weeklyDayStats = weeklyDayStats,
        maxDaySecondsInWeek = maxDaySecondsInWeek,
        mostActiveDayLabel = mostActiveDayLabel,
        allTimeSeconds = allTimeSeconds,
        allTimeSessionsCount = allTimeSessionsCount,
        allTimeBreakSeconds = allTimeBreakSeconds,
        dayGroups = dayGroups
    )
}

private fun generateEmpty7Days(nowMillis: Long): List<DayReadingStat> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStartMillis = cal.timeInMillis
    val oneDayMillis = 24L * 60L * 60L * 1000L

    val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateSubFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val fullDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    return (6 downTo 0).map { dayOffset ->
        val dayStart = todayStartMillis - (dayOffset * oneDayMillis)
        val isToday = (dayOffset == 0)
        val isYesterday = (dayOffset == 1)
        val dayDate = Date(dayStart)

        val displayLabel = when {
            isToday -> "Today"
            isYesterday -> "Yesterday"
            else -> fullDateFormat.format(dayDate)
        }

        DayReadingStat(
            dayOffset = dayOffset,
            dateKey = keyFormat.format(dayDate),
            dayName = dayNameFormat.format(dayDate),
            displayLabel = displayLabel,
            dateSubtitle = dateSubFormat.format(dayDate),
            totalSeconds = 0L,
            sessionCount = 0,
            isToday = isToday
        )
    }
}
