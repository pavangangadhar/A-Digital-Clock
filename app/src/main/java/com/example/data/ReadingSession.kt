package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ReadingSession
 *
 * Entity representing a recorded reading or focus session conducted in full-screen clock mode.
 *
 * Fields:
 * - id: Auto-generated primary key
 * - durationSeconds: Total elapsed reading duration in seconds
 * - startTimeMillis: Timestamp when full-screen mode was entered
 * - endTimeMillis: Timestamp when full-screen mode was exited
 * - note: The active custom note / reminder during the session
 */
@Entity(tableName = "reading_sessions")
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationSeconds: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val note: String = ""
)
