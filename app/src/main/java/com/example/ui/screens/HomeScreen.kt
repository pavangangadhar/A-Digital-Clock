package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.ui.components.BackgroundContainer
import com.example.ui.components.ClockDisplay
import java.time.LocalDateTime

/**
 * HomeScreen
 *
 * The primary dashboard displaying:
 * 1. Live central digital clock (Time + Date) updating every second
 * 2. Large prominent "FULL SCREEN CLOCK" button
 * 3. Quick-navigation buttons for Font, Color, Background, and Settings
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
    onFullScreenClick: () -> Unit,
    onNavigateFont: () -> Unit,
    onNavigateColor: () -> Unit,
    onNavigateBackground: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackgroundContainer(settings = settings, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Digital Clock",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Learning Project",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = settings.fontStyle,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(settings.colorHex)
                    )
                }
            }

            // Central Hero Clock Display (Live Updating)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .testTag("home_clock_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 42.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ClockDisplay(
                        dateTime = dateTime,
                        settings = settings,
                        isFullScreen = false,
                        previewScale = 1.0f
                    )
                }
            }

            // Bottom Actions & Controls Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Prominent FULL SCREEN CLOCK Button
                Button(
                    onClick = onFullScreenClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
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
