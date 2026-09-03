package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ClockRepository
 *
 * Repository layer that acts as the single source of truth for clock configuration.
 *
 * Educational Note:
 * - Decoupling the ViewModel from Room DAO enables cleaner architecture and testing.
 * - If no settings exist yet in the database, we map null to ClockSettings.DEFAULT.
 */
class ClockRepository(private val dao: ClockSettingsDao) {

    val settings: Flow<ClockSettings> = dao.getSettings().map { saved ->
        saved ?: ClockSettings.DEFAULT
    }

    suspend fun updateSettings(settings: ClockSettings) {
        dao.saveSettings(settings)
    }

    suspend fun updateSettings(transform: (ClockSettings) -> ClockSettings, current: ClockSettings) {
        val updated = transform(current)
        dao.saveSettings(updated)
    }
}
