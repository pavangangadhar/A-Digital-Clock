package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ClockDatabase
import com.example.data.ClockRepository
import com.example.data.ClockSettings
import com.example.data.ReadingAnalytics
import com.example.data.ReadingSession
import com.example.data.computeReadingAnalytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    // Reactive flow of all saved reading sessions from Room database
    val readingSessions: StateFlow<List<ReadingSession>>

    // Reactive flow of daily and weekly reading analytics computed from Room reading sessions
    val readingAnalytics: StateFlow<ReadingAnalytics>

    // Live elapsed reading/focus time (in seconds) while in full-screen clock mode
    private val _fullScreenElapsedSeconds = MutableStateFlow(0L)
    val fullScreenElapsedSeconds: StateFlow<Long> = _fullScreenElapsedSeconds.asStateFlow()

    private var fullScreenTimerJob: Job? = null
    private var sessionStartTimeMillis: Long? = null

    // Holds the newly completed session to display the summary dialog upon exiting full screen
    private val _sessionCompletionDialog = MutableStateFlow<ReadingSession?>(null)
    val sessionCompletionDialog: StateFlow<ReadingSession?> = _sessionCompletionDialog.asStateFlow()

    init {
        val database = ClockDatabase.getDatabase(application)
        repository = ClockRepository(database.clockSettingsDao(), database.readingSessionDao())

        // Collect database settings into StateFlow with default initial value
        settings = repository.settings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ClockSettings.DEFAULT
        )

        // Collect reading sessions from Room database
        readingSessions = repository.readingSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Derive daily and weekly reading analytics reactively
        readingAnalytics = repository.readingSessions
            .map { sessions -> computeReadingAnalytics(sessions) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ReadingAnalytics()
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
        sessionStartTimeMillis = System.currentTimeMillis()
        _fullScreenElapsedSeconds.value = 0L

        fullScreenTimerJob?.cancel()
        fullScreenTimerJob = viewModelScope.launch {
            while (isActive && _isFullScreen.value) {
                delay(1000L)
                _fullScreenElapsedSeconds.value += 1L
            }
        }
    }

    fun exitFullScreen() {
        _isFullScreen.value = false
        fullScreenTimerJob?.cancel()
        fullScreenTimerJob = null

        val duration = _fullScreenElapsedSeconds.value
        val start = sessionStartTimeMillis ?: (System.currentTimeMillis() - duration * 1000L)
        val end = System.currentTimeMillis()

        if (duration >= 1L) {
            val sessionToSave = ReadingSession(
                durationSeconds = duration,
                startTimeMillis = start,
                endTimeMillis = end,
                note = if (settings.value.showNote) settings.value.customNote else ""
            )
            viewModelScope.launch {
                val insertedId = repository.saveReadingSession(sessionToSave)
                val savedSessionWithId = sessionToSave.copy(id = insertedId)
                _sessionCompletionDialog.value = savedSessionWithId
            }
        }

        sessionStartTimeMillis = null
        _fullScreenElapsedSeconds.value = 0L
    }

    fun dismissSessionDialog() {
        _sessionCompletionDialog.value = null
    }

    fun deleteReadingSession(id: Long) {
        viewModelScope.launch {
            repository.deleteReadingSession(id)
            if (_sessionCompletionDialog.value?.id == id) {
                _sessionCompletionDialog.value = null
            }
        }
    }

    fun clearAllReadingSessions() {
        viewModelScope.launch {
            repository.clearAllReadingSessions()
            _sessionCompletionDialog.value = null
        }
    }

    fun toggleFullScreen() {
        if (_isFullScreen.value) {
            exitFullScreen()
        } else {
            enterFullScreen()
        }
    }
}
