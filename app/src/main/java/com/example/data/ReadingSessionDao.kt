package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * ReadingSessionDao
 *
 * Data Access Object for querying, saving, and deleting full-screen reading sessions.
 */
@Dao
interface ReadingSessionDao {

    @Query("SELECT * FROM reading_sessions ORDER BY endTimeMillis DESC")
    fun getAllSessions(): Flow<List<ReadingSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSession): Long

    @Query("DELETE FROM reading_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM reading_sessions")
    suspend fun clearAllSessions()

    /**
     * Delete sessions that ended prior to [cutoffMillis].
     * Used for enforcing the 3-month (90-day) history retention limit.
     */
    @Query("DELETE FROM reading_sessions WHERE endTimeMillis < :cutoffMillis")
    suspend fun pruneSessionsOlderThan(cutoffMillis: Long): Int

    /**
     * Query sessions falling within a specific date range [startMillis, endMillis].
     */
    @Query("SELECT * FROM reading_sessions WHERE endTimeMillis BETWEEN :startMillis AND :endMillis ORDER BY endTimeMillis DESC")
    suspend fun getSessionsInRange(startMillis: Long, endMillis: Long): List<ReadingSession>

    /**
     * Count of all retained sessions.
     */
    @Query("SELECT COUNT(*) FROM reading_sessions")
    suspend fun getSessionCount(): Int
}
