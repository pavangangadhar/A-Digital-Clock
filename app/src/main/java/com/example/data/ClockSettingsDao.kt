package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * ClockSettingsDao
 *
 * Data Access Object (DAO) providing database operations for ClockSettings.
 *
 * Educational Note:
 * - Returning Kotlin's Flow<ClockSettings?> means the UI automatically reacts whenever
 *   the settings table changes in the SQLite database.
 * - OnConflictStrategy.REPLACE ensures that inserting a row with id=1 overwrites existing settings.
 */
@Dao
interface ClockSettingsDao {

    @Query("SELECT * FROM clock_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<ClockSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ClockSettings)
}
