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
}
