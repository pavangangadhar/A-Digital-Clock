package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BreakSuggestion
import com.example.data.ClockDatabase
import com.example.data.ClockRepository
import com.example.data.ClockSettings
import com.example.data.ReadingAnalytics
import com.example.data.ReadingBreakState
import com.example.data.ReadingSession
import com.example.data.computeReadingAnalytics
import com.example.data.computeRealTimeBreakSuggestion
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

    // Active reading break state (countdown timer, progress, completion)
    private val _breakState = MutableStateFlow(ReadingBreakState())
    val breakState: StateFlow<ReadingBreakState> = _breakState.asStateFlow()

    // Real-time break suggestion banner prompt (e.g. at 20m, 40m, 60m milestones)
    private val _realTimeBreakPrompt = MutableStateFlow<BreakSuggestion?>(null)
    val realTimeBreakPrompt: StateFlow<BreakSuggestion?> = _realTimeBreakPrompt.asStateFlow()

    private var fullScreenTimerJob: Job? = null
    private var breakTimerJob: Job? = null
    private var sessionStartTimeMillis: Long? = null
    private var lastPromptedMilestoneMinutes: Int = 0

    // Holds the newly completed session to display the summary dialog upon exiting full screen
    private val _sessionCompletionDialog = MutableStateFlow<ReadingSession?>(null)
    val sessionCompletionDialog: StateFlow<ReadingSession?> = _sessionCompletionDialog.asStateFlow()

    // Tracks cumulative break duration taken (in seconds) during the current full-screen session
    private var sessionBreakSeconds: Long = 0L

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

        // Enforce 3-month history retention policy on startup
        viewModelScope.launch {
            repository.pruneSessionsOlderThanThreeMonths()
        }

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
        _breakState.value = ReadingBreakState(isBreakActive = false)
        _realTimeBreakPrompt.value = null
        lastPromptedMilestoneMinutes = 0
        sessionBreakSeconds = 0L

        startReadingTimer()
    }

    private fun startReadingTimer() {
        fullScreenTimerJob?.cancel()
        fullScreenTimerJob = viewModelScope.launch {
            while (isActive && _isFullScreen.value && !_breakState.value.isBreakActive) {
                delay(1000L)
                _fullScreenElapsedSeconds.value += 1L

                // Real-time automatic break prompt at 20 min, 40 min, 60 min reading milestones
                val elapsedMinutes = (_fullScreenElapsedSeconds.value / 60).toInt()
                if (elapsedMinutes >= 20 && elapsedMinutes % 20 == 0 && elapsedMinutes != lastPromptedMilestoneMinutes) {
                    if (!_breakState.value.isBreakActive) {
                        lastPromptedMilestoneMinutes = elapsedMinutes
                        _realTimeBreakPrompt.value = computeRealTimeBreakSuggestion(_fullScreenElapsedSeconds.value)
                    }
                }
            }
        }
    }

    /**
     * Starts a real-time reading break of 5 min, 10 min, or 15 min.
     * Pauses the reading timer so break duration is never counted as reading time.
     */
    fun startBreak(durationMinutes: Int) {
        // Pause active reading timer during break
        fullScreenTimerJob?.cancel()
        _realTimeBreakPrompt.value = null

        val totalSecs = durationMinutes * 60
        _breakState.value = ReadingBreakState(
            isBreakActive = true,
            breakTotalSeconds = totalSecs,
            breakRemainingSeconds = totalSecs,
            isCompleted = false,
            selectedDurationMinutes = durationMinutes
        )

        breakTimerJob?.cancel()
        breakTimerJob = viewModelScope.launch {
            while (isActive && _breakState.value.isBreakActive) {
                delay(1000L)
                sessionBreakSeconds += 1L
                val remaining = _breakState.value.breakRemainingSeconds - 1
                if (remaining <= 0) {
                    _breakState.value = _breakState.value.copy(
                        breakRemainingSeconds = 0,
                        isCompleted = true
                    )
                    break
                } else {
                    _breakState.value = _breakState.value.copy(
                        breakRemainingSeconds = remaining
                    )
                }
            }
        }
    }

    /**
     * Adds extra time to the active break timer (default +60s / 1 min).
     */
    fun addBreakSeconds(seconds: Int = 60) {
        val current = _breakState.value
        val newTotal = current.breakTotalSeconds + seconds
        val newRemaining = current.breakRemainingSeconds + seconds
        val wasCompleted = current.isCompleted

        _breakState.value = current.copy(
            breakTotalSeconds = newTotal,
            breakRemainingSeconds = newRemaining,
            isCompleted = false
        )

        if (wasCompleted || breakTimerJob?.isActive != true) {
            breakTimerJob?.cancel()
            breakTimerJob = viewModelScope.launch {
                while (isActive && _breakState.value.isBreakActive) {
                    delay(1000L)
                    sessionBreakSeconds += 1L
                    val remaining = _breakState.value.breakRemainingSeconds - 1
                    if (remaining <= 0) {
                        _breakState.value = _breakState.value.copy(
                            breakRemainingSeconds = 0,
                            isCompleted = true
                        )
                        break
                    } else {
                        _breakState.value = _breakState.value.copy(
                            breakRemainingSeconds = remaining
                        )
                    }
                }
            }
        }
    }

    /**
     * Resumes reading session from break and restarts the reading timer.
     */
    fun resumeReadingFromBreak() {
        breakTimerJob?.cancel()
        breakTimerJob = null
        _breakState.value = ReadingBreakState(isBreakActive = false)
        // Resume reading timer
        startReadingTimer()
    }

    fun dismissBreakPrompt() {
        _realTimeBreakPrompt.value = null
    }

    fun getBreakSuggestion(): BreakSuggestion {
        return computeRealTimeBreakSuggestion(_fullScreenElapsedSeconds.value)
    }

    fun triggerRealTimeBreakSuggestion() {
        _realTimeBreakPrompt.value = computeRealTimeBreakSuggestion(_fullScreenElapsedSeconds.value)
    }

    fun exitFullScreen() {
        _isFullScreen.value = false
        fullScreenTimerJob?.cancel()
        fullScreenTimerJob = null
        breakTimerJob?.cancel()
        breakTimerJob = null
        _breakState.value = ReadingBreakState(isBreakActive = false)
        _realTimeBreakPrompt.value = null

        val duration = _fullScreenElapsedSeconds.value
        val breakTaken = sessionBreakSeconds
        val start = sessionStartTimeMillis ?: (System.currentTimeMillis() - (duration + breakTaken) * 1000L)
        val end = System.currentTimeMillis()

        if (duration >= 1L) {
            val sessionToSave = ReadingSession(
                durationSeconds = duration,
                startTimeMillis = start,
                endTimeMillis = end,
                note = if (settings.value.showNote) settings.value.customNote else "",
                breakDurationSeconds = breakTaken
            )
            viewModelScope.launch {
                val insertedId = repository.saveReadingSession(sessionToSave)
                val savedSessionWithId = sessionToSave.copy(id = insertedId)
                _sessionCompletionDialog.value = savedSessionWithId
            }
        }

        sessionStartTimeMillis = null
        sessionBreakSeconds = 0L
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
