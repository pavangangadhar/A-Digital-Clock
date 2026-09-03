package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClockDatabase
import com.example.data.ClockRepository
import com.example.data.ClockSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ClockViewModel
 *
 * The central state-management engine for the Digital Clock application.
 *
 * Educational Note:
 * - In Android Jetpack Compose, the ViewModel survives configuration changes (like device rotation).
 * - StateFlow is a reactive state-holder observable by Composables using collectAsStateWithLifecycle().
 * - In Flutter, this is analogous to a ChangeNotifier with notifyListeners() or a ValueNotifier/Bloc.
 */
@SuppressLint("NewApi")
class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClockRepository

    // Reactive flow of user settings loaded from local Room database
    val settings: StateFlow<ClockSettings>

    // Real-time live clock time updated on the second boundary
    private val _currentTime = MutableStateFlow(LocalDateTime.now())
    val currentTime: StateFlow<LocalDateTime> = _currentTime.asStateFlow()

    // Full-screen mode toggle state
    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    init {
        val database = ClockDatabase.getDatabase(application)
        repository = ClockRepository(database.clockSettingsDao())

        // Collect database settings into StateFlow with default initial value
        settings = repository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ClockSettings.DEFAULT
        )

        // Start the continuous second-tick coroutine timer
        startClockTimer()
    }

    /**
     * Coroutine timer that updates _currentTime every second.
     *
     * Educational Note:
     * - We synchronize with the system clock by calculating the remaining milliseconds
     *   to the next whole second: (1000L - System.currentTimeMillis() % 1000L).
     * - This ensures the seconds digit flips at the exact start of every real second!
     */
    private fun startClockTimer() {
        viewModelScope.launch {
            while (isActive) {
                _currentTime.value = LocalDateTime.now()
                val millisUntilNextSecond = 1000L - (System.currentTimeMillis() % 1000L)
                delay(millisUntilNextSecond.coerceAtLeast(50L))
            }
        }
    }

    // --- State Mutation Methods (Each immediately updates the local database) ---

    fun setFont(fontName: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(fontStyle = fontName))
        }
    }

    fun setColor(colorHex: Long) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(colorHex = colorHex))
        }
    }

    fun setSolidBackground(colorHex: Long) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    bgType = ClockSettings.BG_SOLID,
                    bgSolidHex = colorHex
                )
            )
        }
    }

    fun setGradientBackground(gradientIndex: Int) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    bgType = ClockSettings.BG_GRADIENT,
                    bgGradientIndex = gradientIndex
                )
            )
        }
    }

    fun setCustomImageBackground(imageUriString: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    bgType = ClockSettings.BG_IMAGE,
                    customImageUri = imageUriString
                )
            )
        }
    }

    fun set24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(is24Hour = is24Hour))
        }
    }

    fun setShowSeconds(show: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(showSeconds = show))
        }
    }

    fun setShowDate(show: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(showDate = show))
        }
    }

    fun setClockSize(size: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(clockSize = size))
        }
    }

    fun setClockPosition(position: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(clockPosition = position))
        }
    }

    fun setCustomNote(note: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(customNote = note))
        }
    }

    fun setShowNote(show: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(showNote = show))
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.updateSettings(ClockSettings.DEFAULT)
        }
    }

    fun enterFullScreen() {
        _isFullScreen.value = true
    }

    fun exitFullScreen() {
        _isFullScreen.value = false
    }

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }
}
