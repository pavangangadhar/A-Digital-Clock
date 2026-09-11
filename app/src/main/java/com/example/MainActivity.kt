package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ClockViewModel
import com.example.ui.components.ReadingSessionDialog
import com.example.ui.screens.BackgroundScreen
import com.example.ui.screens.ColorScreen
import com.example.ui.screens.FontScreen
import com.example.ui.screens.FullScreenClockScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReadingHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

/**
 * Screen destinations for beginner-friendly navigation
 */
enum class AppScreen {
    HOME,
    FONT,
    COLOR,
    BACKGROUND,
    SETTINGS,
    READING_HISTORY,
    FULL_SCREEN
}

/**
 * MainActivity
 *
 * The main Android Activity hosting the Jetpack Compose user interface.
 *
 * Educational Note:
 * - In Android, the Activity is the single window hosting Compose UI via setContent { }.
 * - In Flutter, the equivalent entry point is void main() => runApp(const MyApp()).
 * - BackHandler intercepts system hardware/gesture back events cleanly without boilerplate.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                DigitalClockApp()
            }
        }
    }
}

@Composable
fun DigitalClockApp(
    viewModel: ClockViewModel = viewModel()
) {
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val readingSessions by viewModel.readingSessions.collectAsStateWithLifecycle()
    val readingAnalytics by viewModel.readingAnalytics.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.fullScreenElapsedSeconds.collectAsStateWithLifecycle()
    val sessionCompletionDialog by viewModel.sessionCompletionDialog.collectAsStateWithLifecycle()

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }

    // Intercept back button when not on HOME screen
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            if (currentScreen == AppScreen.FULL_SCREEN) {
                viewModel.exitFullScreen()
            }
            currentScreen = AppScreen.HOME
        }
    }

    // Modal Dialog shown immediately when exiting full screen to show how much time was read
    sessionCompletionDialog?.let { session ->
        ReadingSessionDialog(
            session = session,
            settings = settings,
            todayTotalSeconds = readingAnalytics.todaySeconds,
            weekTotalSeconds = readingAnalytics.weekSeconds,
            onDismiss = { viewModel.dismissSessionDialog() },
            onDelete = { id -> viewModel.deleteReadingSession(id) },
            onViewHistory = {
                viewModel.dismissSessionDialog()
                currentScreen = AppScreen.READING_HISTORY
            }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            animationSpec = tween(250),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        dateTime = currentTime,
                        settings = settings,
                        sessionCount = readingSessions.size,
                        totalReadingSeconds = readingSessions.sumOf { it.durationSeconds },
                        todayReadingSeconds = readingAnalytics.todaySeconds,
                        weekReadingSeconds = readingAnalytics.weekSeconds,
                        onFullScreenClick = {
                            viewModel.enterFullScreen()
                            currentScreen = AppScreen.FULL_SCREEN
                        },
                        onNavigateHistory = { currentScreen = AppScreen.READING_HISTORY },
                        onNavigateFont = { currentScreen = AppScreen.FONT },
                        onNavigateColor = { currentScreen = AppScreen.COLOR },
                        onNavigateBackground = { currentScreen = AppScreen.BACKGROUND },
                        onNavigateSettings = { currentScreen = AppScreen.SETTINGS },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                AppScreen.FONT -> {
                    FontScreen(
                        dateTime = currentTime,
                        settings = settings,
                        onFontSelected = { font ->
                            viewModel.setFont(font)
                        },
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.COLOR -> {
                    ColorScreen(
                        dateTime = currentTime,
                        settings = settings,
                        onColorSelected = { colorHex ->
                            viewModel.setColor(colorHex)
                        },
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.BACKGROUND -> {
                    BackgroundScreen(
                        dateTime = currentTime,
                        settings = settings,
                        onSolidSelected = { colorHex ->
                            viewModel.setSolidBackground(colorHex)
                        },
                        onGradientSelected = { index ->
                            viewModel.setGradientBackground(index)
                        },
                        onImageSelected = { uri ->
                            viewModel.setCustomImageBackground(uri)
                        },
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        dateTime = currentTime,
                        settings = settings,
                        on24HourToggle = { is24 -> viewModel.set24HourFormat(is24) },
                        onSecondsToggle = { show -> viewModel.setShowSeconds(show) },
                        onDateToggle = { show -> viewModel.setShowDate(show) },
                        onCustomNoteChange = { note -> viewModel.setCustomNote(note) },
                        onShowNoteToggle = { show -> viewModel.setShowNote(show) },
                        onSizeSelected = { size -> viewModel.setClockSize(size) },
                        onPositionSelected = { pos -> viewModel.setClockPosition(pos) },
                        onResetDefaults = { viewModel.resetToDefaults() },
                        onNavigateHistory = { currentScreen = AppScreen.READING_HISTORY },
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.READING_HISTORY -> {
                    ReadingHistoryScreen(
                        sessions = readingSessions,
                        settings = settings,
                        onDeleteSession = { id -> viewModel.deleteReadingSession(id) },
                        onClearAllSessions = { viewModel.clearAllReadingSessions() },
                        onStartFullScreen = {
                            viewModel.enterFullScreen()
                            currentScreen = AppScreen.FULL_SCREEN
                        },
                        onBack = { currentScreen = AppScreen.HOME }
                    )
                }

                AppScreen.FULL_SCREEN -> {
                    FullScreenClockScreen(
                        dateTime = currentTime,
                        settings = settings,
                        elapsedSeconds = elapsedSeconds,
                        onExit = {
                            viewModel.exitFullScreen()
                            currentScreen = AppScreen.HOME
                        }
                    )
                }
            }
        }
    }
}

/**
 * Greeting Composable maintained for tests and previews.
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
