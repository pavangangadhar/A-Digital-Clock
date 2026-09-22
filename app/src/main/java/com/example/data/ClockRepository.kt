package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

/**
 * ClockRepository
 *
 * Repository layer that acts as the single source of truth for clock configuration.
 *
 * Educational Note:
 * - Decoupling the ViewModel from Room DAO enables cleaner architecture and testing.
 * - If no settings exist yet in the database, we map null to ClockSettings.DEFAULT.
 * - Enforces the 3-month (90-day) retention window for reading history and analytics.
 */
class ClockRepository(
    private val dao: ClockSettingsDao,
    private val sessionDao: ReadingSessionDao
) {

    companion object {
        const val RETENTION_MONTHS = 3
        const val RETENTION_DAYS = 90

        /**
         * Calculates the cutoff timestamp for 3-month retention.
         * Any session ending before this timestamp is eligible for pruning.
         */
        fun calculateThreeMonthCutoffMillis(nowMillis: Long = System.currentTimeMillis()): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.MONTH, -RETENTION_MONTHS)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }

    val settings: Flow<ClockSettings> = dao.getSettings().map { saved ->
        saved ?: ClockSettings.DEFAULT
    }

    val readingSessions: Flow<List<ReadingSession>> = sessionDao.getAllSessions()

    suspend fun updateSettings(settings: ClockSettings) {
        dao.saveSettings(settings)
    }

    suspend fun updateSettings(transform: (ClockSettings) -> ClockSettings, current: ClockSettings) {
        val updated = transform(current)
        dao.saveSettings(updated)
    }

    /**
     * Saves a reading session and automatically purges records older than 3 months.
     */
    suspend fun saveReadingSession(session: ReadingSession): Long {
        pruneSessionsOlderThanThreeMonths()
        return sessionDao.insertSession(session)
    }

    /**
     * Prunes reading sessions older than 3 calendar months.
     * Returns the count of pruned records.
     */
    suspend fun pruneSessionsOlderThanThreeMonths(nowMillis: Long = System.currentTimeMillis()): Int {
        val cutoff = calculateThreeMonthCutoffMillis(nowMillis)
        return sessionDao.pruneSessionsOlderThan(cutoff)
    }

    /**
     * Fetches sessions within a specific date range for analytics and PDF export.
     */
    suspend fun getSessionsInRange(startMillis: Long, endMillis: Long): List<ReadingSession> {
        return sessionDao.getSessionsInRange(startMillis, endMillis)
    }

    suspend fun deleteReadingSession(id: Long) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun clearAllReadingSessions() {
        sessionDao.clearAllSessions()
    }
}
