package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * ClockDatabase
 *
 * Local SQLite database created via Android Room.
 *
 * Educational Note:
 * - In Android, Room is an abstraction over SQLite that verifies SQL queries at compile-time.
 * - In Flutter, equivalent local storage is typically handled via SharedPreferences or Hive/Isar.
 * - We use a singleton pattern here so only one database instance is active at any time.
 */
@Database(entities = [ClockSettings::class, ReadingSession::class], version = 3, exportSchema = false)
abstract class ClockDatabase : RoomDatabase() {

    abstract fun clockSettingsDao(): ClockSettingsDao
    abstract fun readingSessionDao(): ReadingSessionDao

    companion object {
        @Volatile
        private var INSTANCE: ClockDatabase? = null

        fun getDatabase(context: Context): ClockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClockDatabase::class.java,
                    "clock_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
