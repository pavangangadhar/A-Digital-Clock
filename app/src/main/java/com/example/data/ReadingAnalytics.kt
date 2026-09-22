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
    val dayGroups: List<DayGroupedSessions> = emptyList(),
    val retentionMonths: Int = 3,
    val retentionDays: Int = 90
)

/**
 * DateRangePreset
 *
 * Predefined range options for reading history analysis and PDF export.
 */
enum class DateRangePreset(val label: String, val shortLabel: String) {
    LAST_7_DAYS("Last 7 Days", "7 Days"),
    LAST_30_DAYS("Last 30 Days", "30 Days"),
    LAST_3_MONTHS("Last 3 Months (Full History)", "3 Months"),
    TODAY("Today", "Today"),
    CUSTOM("Custom Date Range", "Custom")
}

/**
 * Represents a selected time boundary for filtering and PDF export.
 */
data class DateRangeSelection(
    val preset: DateRangePreset = DateRangePreset.LAST_30_DAYS,
    val startMillis: Long,
    val endMillis: Long
) {
    val formattedLabel: String
        get() {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            return "${sdf.format(Date(startMillis))} – ${sdf.format(Date(endMillis))}"
        }

    val daysCount: Int
        get() = (((endMillis - startMillis) / (24L * 60L * 60L * 1000L)).toInt() + 1).coerceAtLeast(1)
}

/**
 * Encapsulates the analytics for an arbitrary date range selection,
 * used for previewing and generating the PDF report.
 */
data class RangeAnalytics(
    val dateRange: DateRangeSelection,
    val totalSessions: Int,
    val totalReadingSeconds: Long,
    val totalBreakSeconds: Long,
    val activeDaysCount: Int,
    val dailyAverageReadingSeconds: Long,
    val longestSessionSeconds: Long,
    val sessions: List<ReadingSession>,
    val daySummaries: List<DayGroupedSessions>
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

/**
 * Creates a DateRangeSelection according to the requested preset or custom boundaries.
 */
fun createDateRangeSelection(
    preset: DateRangePreset,
    customStartMillis: Long? = null,
    customEndMillis: Long? = null,
    nowMillis: Long = System.currentTimeMillis()
): DateRangeSelection {
    val cal = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val endOfToday = cal.timeInMillis

    cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = cal.timeInMillis

    val oneDayMillis = 24L * 60L * 60L * 1000L

    return when (preset) {
        DateRangePreset.TODAY -> DateRangeSelection(
            preset = preset,
            startMillis = startOfToday,
            endMillis = endOfToday
        )
        DateRangePreset.LAST_7_DAYS -> DateRangeSelection(
            preset = preset,
            startMillis = (startOfToday - 6L * oneDayMillis),
            endMillis = endOfToday
        )
        DateRangePreset.LAST_30_DAYS -> DateRangeSelection(
            preset = preset,
            startMillis = (startOfToday - 29L * oneDayMillis),
            endMillis = endOfToday
        )
        DateRangePreset.LAST_3_MONTHS -> {
            val startCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.MONTH, -3)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            DateRangeSelection(
                preset = preset,
                startMillis = startCal.timeInMillis,
                endMillis = endOfToday
            )
        }
        DateRangePreset.CUSTOM -> {
            val start = customStartMillis ?: (startOfToday - 29L * oneDayMillis)
            val end = customEndMillis ?: endOfToday
            DateRangeSelection(
                preset = preset,
                startMillis = minOf(start, end),
                endMillis = maxOf(start, end)
            )
        }
    }
}

/**
 * Computes analytics and aggregated daily summaries for any selected DateRangeSelection.
 */
fun computeRangeAnalytics(
    sessions: List<ReadingSession>,
    selection: DateRangeSelection
): RangeAnalytics {
    val filtered = sessions.filter { it.endTimeMillis in selection.startMillis..selection.endMillis }
    val totalReading = filtered.sumOf { it.durationSeconds }
    val totalBreaks = filtered.sumOf { it.breakDurationSeconds }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    val groupedByDate = filtered
        .groupBy { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.endTimeMillis }
            dateFormat.format(cal.time)
        }
        .toSortedMap(compareByDescending { it })

    val daySummaries = groupedByDate.map { (key, daySessions) ->
        val firstCal = Calendar.getInstance().apply { timeInMillis = daySessions.first().endTimeMillis }
        val label = labelFormat.format(firstCal.time)
        DayGroupedSessions(
            dateKey = key,
            dateLabel = label,
            totalSeconds = daySessions.sumOf { it.durationSeconds },
            sessionCount = daySessions.size,
            sessions = daySessions,
            totalBreakSeconds = daySessions.sumOf { it.breakDurationSeconds }
        )
    }

    val daysCount = selection.daysCount.coerceAtLeast(1)
    val dailyAvg = totalReading / daysCount
    val longest = filtered.maxOfOrNull { it.durationSeconds } ?: 0L

    return RangeAnalytics(
        dateRange = selection,
        totalSessions = filtered.size,
        totalReadingSeconds = totalReading,
        totalBreakSeconds = totalBreaks,
        activeDaysCount = daySummaries.size,
        dailyAverageReadingSeconds = dailyAvg,
        longestSessionSeconds = longest,
        sessions = filtered,
        daySummaries = daySummaries
    )
}

