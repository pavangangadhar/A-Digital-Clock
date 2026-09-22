package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.BreakSuggestion
import com.example.data.ClockSettings
import com.example.data.ReadingBreakState
import com.example.data.computeRealTimeBreakSuggestion
import com.example.ui.components.BackgroundContainer
import com.example.ui.components.BreakOptionsDialog
import com.example.ui.components.BreakPromptBanner
import com.example.ui.components.ClockDisplay
import com.example.ui.components.ReadingBreakOverlay
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * FullScreenClockScreen
 *
 * Immersive full-screen digital clock display with real-time reading session tracking,
 * quick break options (5 min, 10 min, 15 min), and real-time intelligent break suggestions.
 */
@SuppressLint("NewApi")
@Composable
fun FullScreenClockScreen(
    dateTime: LocalDateTime,
    settings: ClockSettings,
    elapsedSeconds: Long = 0L,
    breakState: ReadingBreakState = ReadingBreakState(),
    realTimePrompt: BreakSuggestion? = null,
    onStartBreak: (Int) -> Unit = {},
    onResumeReading: () -> Unit = {},
    onAddOneMinute: () -> Unit = {},
    onDismissPrompt: () -> Unit = {},
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Manage Keep-Screen-Awake & Immersive System Bars
    DisposableEffect(activity) {
        val window = activity?.window
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            val win = activity?.window
            if (win != null) {
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                val insetsController = WindowCompat.getInsetsController(win, win.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // If a break is currently active, show the immersive Break Overlay directly
    if (breakState.isBreakActive) {
        ReadingBreakOverlay(
            breakState = breakState,
            accentColorHex = settings.colorHex,
            onResumeReading = onResumeReading,
            onAddOneMinute = onAddOneMinute,
            onExitSession = onExit,
            modifier = modifier
        )
        return
    }

    // State for opening full break options & recommendation dialog
    var showBreakOptionsDialog by remember { mutableStateOf(false) }

    // Dynamic real-time break recommendation based on current reading elapsed duration
    val currentSuggestion = remember(elapsedSeconds) {
        computeRealTimeBreakSuggestion(elapsedSeconds)
    }

    // Temporary hint banner that auto-fades after 4 seconds
    var showHint by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(4000)
        showHint = false
    }

    // Pulsing animation for the active session indicator dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Break Options Dialog
    if (showBreakOptionsDialog) {
        BreakOptionsDialog(
            suggestion = currentSuggestion,
            accentColorHex = settings.colorHex,
            onSelectDuration = { duration ->
                showBreakOptionsDialog = false
                onStartBreak(duration)
            },
            onDismiss = { showBreakOptionsDialog = false }
        )
    }

    BackgroundContainer(
        settings = settings,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onExit()
                    }
                )
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Center Reading Status & Break Control Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Reading Session Counter Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.58f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.testTag("fullscreen_reading_timer")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pulsing active recording indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alpha(pulseAlpha)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Reading",
                            tint = Color(settings.colorHex),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reading: ${formatElapsedTimer(elapsedSeconds)}",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Quick Break Options Toolbar: [ ☕ Break ] [ 5m ] [ 10m ] [ 15m ] [ ℹ️ Suggestion ]
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, Color(settings.colorHex).copy(alpha = 0.35f)),
                    modifier = Modifier.testTag("fullscreen_break_toolbar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // "Break" title pill that opens full options dialog
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showBreakOptionsDialog = true }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = "Break Options",
                                tint = Color(settings.colorHex),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Break:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // 5 MIN QUICK CHIP
                        BreakQuickChip(
                            label = "5 min",
                            isSuggested = (currentSuggestion.durationMinutes == 5),
                            accentColor = Color(settings.colorHex),
                            onClick = { onStartBreak(5) },
                            testTag = "quick_break_5m_btn"
                        )

                        // 10 MIN QUICK CHIP
                        BreakQuickChip(
                            label = "10 min",
                            isSuggested = (currentSuggestion.durationMinutes == 10),
                            accentColor = Color(settings.colorHex),
                            onClick = { onStartBreak(10) },
                            testTag = "quick_break_10m_btn"
                        )

                        // 15 MIN QUICK CHIP
                        BreakQuickChip(
                            label = "15 min",
                            isSuggested = (currentSuggestion.durationMinutes == 15),
                            accentColor = Color(settings.colorHex),
                            onClick = { onStartBreak(15) },
                            testTag = "quick_break_15m_btn"
                        )

                        // Suggestion info button
                        IconButton(
                            onClick = { showBreakOptionsDialog = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("open_break_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Break suggestion details",
                                tint = Color(settings.colorHex).copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Center Live Clock
            ClockDisplay(
                dateTime = dateTime,
                settings = settings,
                isFullScreen = true,
                previewScale = 1.0f,
                modifier = Modifier.align(Alignment.Center)
            )

            // Top-Right Unobtrusive Exit Button
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .testTag("exit_fullscreen_btn")
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Full Screen",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom Real-Time Break Prompt Banner (Appears at reading milestones or upon suggestion)
            AnimatedVisibility(
                visible = (realTimePrompt != null),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                realTimePrompt?.let { prompt ->
                    BreakPromptBanner(
                        suggestion = prompt,
                        accentColorHex = settings.colorHex,
                        onAcceptSuggestedBreak = { minutes -> onStartBreak(minutes) },
                        onOpenOptions = { showBreakOptionsDialog = true },
                        onDismiss = onDismissPrompt
                    )
                }
            }

            // Bottom Gentle Hint Banner (Fades out automatically if no break prompt is showing)
            AnimatedVisibility(
                visible = (showHint && realTimePrompt == null),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Double-tap anywhere or tap ✕ to exit • Tap 5m/10m/15m for reading break",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact Quick Break Chip in the full-screen toolbar
 */
@Composable
private fun BreakQuickChip(
    label: String,
    isSuggested: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSuggested) accentColor.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f),
        border = if (isSuggested) BorderStroke(1.dp, accentColor) else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSuggested) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Suggested",
                    tint = accentColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSuggested) FontWeight.Bold else FontWeight.Medium,
                color = if (isSuggested) accentColor else Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Format total elapsed seconds into HH:MM:SS or MM:SS
 */
private fun formatElapsedTimer(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
