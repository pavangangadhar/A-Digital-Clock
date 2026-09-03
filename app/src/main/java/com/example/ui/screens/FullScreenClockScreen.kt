package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.ClockSettings
import com.example.ui.components.BackgroundContainer
import com.example.ui.components.ClockDisplay
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * FullScreenClockScreen
 *
 * Immersive full-screen digital clock display.
 *
 * Educational Notes on Screen-Awake & Immersive Mode:
 *
 * 1. KEEP SCREEN AWAKE (Android vs Flutter/iOS):
 *    - Android Native: WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON is applied to the Activity window.
 *      Unlike WakeLocks, FLAG_KEEP_SCREEN_ON does not require WAKE_LOCK permissions and is managed
 *      automatically by the window manager when the app loses focus or is closed.
 *      Using DisposableEffect ensures the flag is added on enter and cleared immediately on exit!
 *    - Flutter / Cross-Platform: In Flutter, packages like `wakelock_plus` or `keep_screen_on` invoke
 *      this exact native Window flag on Android and `UIApplication.shared.isIdleTimerDisabled = true` on iOS!
 *
 * 2. HIDING SYSTEM BARS (Immersive Mode):
 *    - WindowInsetsControllerCompat hides both status bar and navigation bar.
 *    - BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE allows transient access by swiping from the edge.
 *
 * 3. EXIT INTERACTION:
 *    - Double-tap gesture anywhere on the screen triggers onExit().
 *    - An unobtrusive floating exit icon in the corner provides an accessible touch affordance.
 */
@SuppressLint("NewApi")
@Composable
fun FullScreenClockScreen(
    dateTime: LocalDateTime,
    settings: ClockSettings,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Manage Keep-Screen-Awake & Immersive System Bars
    DisposableEffect(activity) {
        val window = activity?.window
        if (window != null) {
            // Keep screen on while full-screen clock is active
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            // Hide system navigation bar and status bar
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            val win = activity?.window
            if (win != null) {
                // Return device to normal sleep behavior immediately
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                // Restore system bars
                val insetsController = WindowCompat.getInsetsController(win, win.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Temporary hint banner that auto-fades after 3.5 seconds
    var showHint by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3500)
        showHint = false
    }

    BackgroundContainer(
        settings = settings,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Double-tap gesture to exit full screen mode
                detectTapGestures(
                    onDoubleTap = {
                        onExit()
                    }
                )
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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

            // Bottom Gentle Hint Banner (Fades out automatically)
            AnimatedVisibility(
                visible = showHint,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Text(
                        text = "Double-tap anywhere to exit",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
