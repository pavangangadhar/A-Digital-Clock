package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.ui.components.ClockDisplay
import java.time.LocalDateTime

/**
 * Preset clock colors required by the prompt
 */
val PRESET_CLOCK_COLORS = listOf(
    0xFFFFFFFF to "White",
    0xFF404040 to "Black / Dark",
    0xFFFF3B30 to "Red",
    0xFF007AFF to "Blue",
    0xFF34C759 to "Green",
    0xFFFFCC00 to "Yellow",
    0xFFAF52DE to "Purple",
    0xFFFF9500 to "Orange",
    0xFF00E5FF to "Cyan",
    0xFF39FF14 to "Neon Green",
    0xFFFF2D55 to "Hot Pink",
    0xFFFFB300 to "Amber"
)

/**
 * ColorScreen
 *
 * Educational screen providing:
 * 1. Live preview card of current time in selected color
 * 2. Preset color swatches (White, Black, Red, Blue, Green, Yellow, Purple, Orange, Cyan, etc.)
 * 3. Interactive Custom Color Picker (Hue, Saturation, Value sliders)
 *
 * Educational Note:
 * - Color in mobile UI is commonly represented as 32-bit ARGB (Alpha, Red, Green, Blue).
 * - For user-friendly custom color picking, HSV (Hue 0-360°, Saturation 0-1, Value 0-1) is
 *   significantly more intuitive than raw RGB because Hue corresponds directly to the color wheel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("NewApi")
@Composable
fun ColorScreen(
    dateTime: LocalDateTime,
    settings: ClockSettings,
    onColorSelected: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sliders state for custom HSV picker
    var hue by remember { mutableFloatStateOf(180f) }
    var saturation by remember { mutableFloatStateOf(1.0f) }
    var value by remember { mutableFloatStateOf(1.0f) }

    val customColor = remember(hue, saturation, value) {
        val hsv = floatArrayOf(hue, saturation, value)
        val argb = android.graphics.Color.HSVToColor(hsv)
        Color(argb)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0D1117),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clock Color",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("color_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1117)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ClockDisplay(
                        dateTime = dateTime,
                        settings = settings,
                        isFullScreen = false,
                        previewScale = 0.85f
                    )
                }
            }

            // Section 1: Presets
            Text(
                text = "Preset Colors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PRESET_CLOCK_COLORS.forEach { (colorValue, name) ->
                    val isSelected = settings.colorHex == colorValue
                    val swatchColor = Color(colorValue)

                    Surface(
                        onClick = { onColorSelected(colorValue) },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("preset_color_$name"),
                        shape = CircleShape,
                        color = swatchColor,
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(3.dp, Color.White)
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        }
                    ) {
                        if (isSelected) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = if (isLightColor(colorValue)) Color.Black else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Custom Color Picker (HSV)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Color Picker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Swatch & Hex preview
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(customColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("#%06X", (0xFFFFFF and customColor.toArgb())),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                    }

                    // Hue Slider
                    Text(
                        text = "Hue (${hue.toInt()}°)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            val hsv = floatArrayOf(hue, saturation, value)
                            val argb = android.graphics.Color.HSVToColor(hsv)
                            onColorSelected((argb.toLong() and 0xFFFFFFFFL))
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = customColor,
                            activeTrackColor = customColor
                        )
                    )

                    // Saturation Slider
                    Text(
                        text = "Saturation (${(saturation * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            val hsv = floatArrayOf(hue, saturation, value)
                            val argb = android.graphics.Color.HSVToColor(hsv)
                            onColorSelected((argb.toLong() and 0xFFFFFFFFL))
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = customColor,
                            activeTrackColor = customColor
                        )
                    )

                    // Brightness (Value) Slider
                    Text(
                        text = "Brightness (${(value * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Slider(
                        value = value,
                        onValueChange = {
                            value = it
                            val hsv = floatArrayOf(hue, saturation, value)
                            val argb = android.graphics.Color.HSVToColor(hsv)
                            onColorSelected((argb.toLong() and 0xFFFFFFFFL))
                        },
                        valueRange = 0.2f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = customColor,
                            activeTrackColor = customColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
