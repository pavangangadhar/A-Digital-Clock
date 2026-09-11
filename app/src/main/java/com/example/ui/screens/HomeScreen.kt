package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.ui.components.BackgroundContainer
import com.example.ui.components.ClockDisplay
import com.example.ui.components.formatReadingDuration
import java.time.LocalDateTime

/**
 * HomeScreen
 *
 * The primary dashboard displaying:
 * 1. Live central digital clock (Time + Date) updating every second
 * 2. Reading time tracker quick card & summary
 * 3. Double-tap gesture on running time to enter Full Screen mode
 * 4. Small button on running time card and top header to quickly enter Full Screen
 * 5. Full support for screen rotation and landscape orientation
 * 6. Quick-navigation buttons for Font, Color, Background, and Settings
 *
 * Educational Note:
 * - Stateless composable: receives state (dateTime, settings) and event lambdas.
 * - Adheres strictly to unidirectional data flow (UDF).
 */
@SuppressLint("NewApi")
@Composable
fun HomeScreen(
    dateTime: LocalDateTime,
    settings: ClockSettings,
    sessionCount: Int = 0,
    totalReadingSeconds: Long = 0L,
    todayReadingSeconds: Long = 0L,
    weekReadingSeconds: Long = 0L,
    onFullScreenClick: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateFont: () -> Unit,
    onNavigateColor: () -> Unit,
    onNavigateBackground: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    BackgroundContainer(settings = settings, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    horizontal = if (isLandscape) 16.dp else 20.dp,
                    vertical = if (isLandscape) 8.dp else 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) Arrangement.spacedBy(10.dp) else Arrangement.SpaceBetween
        ) {
            // Top App Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isLandscape) 4.dp else 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Digital Clock",
                        style = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isLandscape) "Rotate / Double-tap clock for Full Screen" else "Learning Project",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Full Screen Button in Top Bar
                    Surface(
                        onClick = onFullScreenClick,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(settings.colorHex).copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color(settings.colorHex).copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("nav_btn_fullscreen_top")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Full Screen",
                                tint = Color(settings.colorHex),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Full Screen",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Reading History Quick Pill
                    Surface(
                        onClick = onNavigateHistory,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.testTag("nav_btn_reading_history_top")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = "Reading History",
                                tint = Color(settings.colorHex),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sessionCount > 0) "$sessionCount Read" else "History",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }

                    // Font Style Indicator Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = settings.fontStyle,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(settings.colorHex)
                        )
                    }
                }
            }

            // Central Hero Clock Display (Live Updating with Double-Tap & Small Full-Screen Button)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (isLandscape) 6.dp else 12.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                onFullScreenClick()
                            }
                        )
                    }
                    .testTag("home_clock_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.38f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = if (isLandscape) 18.dp else 36.dp,
                            horizontal = 12.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Small Full-Screen Button in the top-right corner of the running time card
                    IconButton(
                        onClick = onFullScreenClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .testTag("small_fullscreen_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(settings.colorHex).copy(alpha = 0.5f)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Enter Full Screen",
                                    tint = Color(settings.colorHex),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Main running time clock display
                    ClockDisplay(
                        dateTime = dateTime,
                        settings = settings,
                        isFullScreen = false,
                        previewScale = if (isLandscape) 0.95f else 1.0f
                    )

                    // Subtle double-tap hint banner at bottom of running time
                    Surface(
                        onClick = onFullScreenClick,
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.45f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .testTag("double_tap_hint_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color(settings.colorHex).copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Double-tap running time for Full Screen",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Bottom Actions & Controls Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clickable Reading Tracker Summary Banner
                Card(
                    onClick = onNavigateHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_reading_history_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.28f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(settings.colorHex).copy(alpha = 0.2f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoStories,
                                        contentDescription = null,
                                        tint = Color(settings.colorHex),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Reading Time Tracker",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                if (sessionCount > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(settings.colorHex).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Today: ${formatReadingDuration(todayReadingSeconds)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(settings.colorHex),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.White.copy(alpha = 0.08f)
                                        ) {
                                            Text(
                                                text = "Week: ${formatReadingDuration(weekReadingSeconds)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White.copy(alpha = 0.85f),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Full screen tracks your reading time • Tap for analysis",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "View History",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Large Prominent FULL SCREEN CLOCK Button
                Button(
                    onClick = onFullScreenClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("full_screen_clock_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(settings.colorHex)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    val contentColor = if (isLightColor(settings.colorHex)) Color.Black else Color.White
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Full Screen",
                        tint = contentColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FULL SCREEN CLOCK",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = contentColor
                    )
                }

                // 4 Customization Category Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NavQuickButton(
                        icon = Icons.Default.TextFields,
                        label = "Font",
                        onClick = onNavigateFont,
                        modifier = Modifier.weight(1f)
                    )
                    NavQuickButton(
                        icon = Icons.Default.ColorLens,
                        label = "Color",
                        onClick = onNavigateColor,
                        modifier = Modifier.weight(1f)
                    )
                    NavQuickButton(
                        icon = Icons.Default.Image,
                        label = "Background",
                        onClick = onNavigateBackground,
                        modifier = Modifier.weight(1f)
                    )
                    NavQuickButton(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = onNavigateSettings,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Reusable rounded card button for navigation.
 */
@Composable
private fun NavQuickButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(72.dp)
            .testTag("nav_btn_$label"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.08f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Utility to calculate luminance and guarantee text readability on colored buttons.
 */
fun isLightColor(colorHex: Long): Boolean {
    val r = ((colorHex shr 16) and 0xFF) / 255.0
    val g = ((colorHex shr 8) and 0xFF) / 255.0
    val b = (colorHex and 0xFF) / 255.0
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
    return luminance > 0.5
}

fun isLightColor(color: Color): Boolean {
    val luminance = 0.2126 * color.red + 0.7152 * color.green + 0.0722 * color.blue
    return luminance > 0.5
}
