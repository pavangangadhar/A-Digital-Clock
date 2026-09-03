package com.example.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ClockDisplay
 *
 * The core educational clock display component.
 *
 * Educational Note:
 * - Demonstrates conditional styling based on user settings.
 * - Handles 12-hour vs 24-hour time formatting via java.time.DateTimeFormatter.
 * - Dynamically computes font families, weights, tracking, and font sizes.
 */
@SuppressLint("NewApi")
@Composable
fun ClockDisplay(
    dateTime: LocalDateTime,
    settings: ClockSettings,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    previewScale: Float = 1.0f
) {
    val clockColor = Color(settings.colorHex)
    val dateColor = clockColor.copy(alpha = 0.85f)

    // Format time components
    val timePattern = buildString {
        if (settings.is24Hour) {
            append("HH:mm")
        } else {
            append("hh:mm")
        }
        if (settings.showSeconds) {
            append(":ss")
        }
        if (!settings.is24Hour) {
            append(" a")
        }
    }
    val formattedTime = dateTime.format(DateTimeFormatter.ofPattern(timePattern))

    // Format date component
    val datePattern = "EEEE, MMMM d, yyyy"
    val formattedDate = dateTime.format(DateTimeFormatter.ofPattern(datePattern))

    // Determine font sizes based on size setting & full screen flag
    val (timeFontSize, dateFontSize) = getFontSizes(settings.clockSize, isFullScreen, previewScale)

    // Positioning alignment
    val verticalArrangement = when (settings.clockPosition) {
        ClockSettings.POS_TOP -> Arrangement.Top
        ClockSettings.POS_BOTTOM -> Arrangement.Bottom
        else -> Arrangement.Center
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalArrangement
    ) {
        if (settings.fontStyle == ClockSettings.FONT_SEVEN_SEGMENT) {
            // Render genuine 7-segment digital LED/LCD display
            val scaleFactor = when (settings.clockSize) {
                ClockSettings.SIZE_SMALL -> 0.70f
                ClockSettings.SIZE_MEDIUM -> 0.90f
                ClockSettings.SIZE_LARGE -> 1.15f
                ClockSettings.SIZE_XLARGE -> 1.40f
                else -> 1.0f
            } * previewScale * (if (isFullScreen) 1.25f else 1.0f)

            SevenSegmentClockView(
                timeString = formattedTime,
                activeColor = clockColor,
                digitWidth = (36 * scaleFactor).dp,
                digitHeight = (68 * scaleFactor).dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            // Render text with corresponding typography style
            val textStyle = getClockTextStyle(settings.fontStyle, timeFontSize, clockColor)

            Text(
                text = formattedTime,
                style = textStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Optional Date Display
        if (settings.showDate) {
            Spacer(modifier = Modifier.height(10.dp * previewScale))
            Text(
                text = formattedDate,
                style = getDateTextStyle(settings.fontStyle, dateFontSize, dateColor),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Custom Note Display
        if (settings.showNote && settings.customNote.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp * previewScale))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, clockColor.copy(alpha = 0.35f)),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("clock_custom_note_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = settings.customNote,
                        style = getNoteTextStyle(settings.fontStyle, dateFontSize * 0.95f, clockColor),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Maps each of the 10 font styles to distinct typography styles.
 */
fun getClockTextStyle(fontStyle: String, fontSize: TextUnit, color: Color): TextStyle {
    return when (fontStyle) {
        ClockSettings.FONT_DIGITAL -> TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            letterSpacing = 3.sp,
            color = color
        )
        ClockSettings.FONT_MINIMAL -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light,
            fontSize = fontSize,
            letterSpacing = 1.sp,
            color = color
        )
        ClockSettings.FONT_THIN -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Thin,
            fontSize = fontSize,
            letterSpacing = 0.5.sp,
            color = color
        )
        ClockSettings.FONT_BOLD -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = fontSize,
            letterSpacing = (-1).sp,
            color = color
        )
        ClockSettings.FONT_FUTURISTIC -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize,
            letterSpacing = 6.sp,
            color = color
        )
        ClockSettings.FONT_ROUNDED -> TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            letterSpacing = 2.sp,
            color = color
        )
        ClockSettings.FONT_RETRO -> TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = 4.sp,
            color = color
        )
        ClockSettings.FONT_MONOSPACE -> TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            letterSpacing = 1.sp,
            color = color
        )
        ClockSettings.FONT_SERIF -> TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            fontSize = fontSize,
            letterSpacing = 1.5.sp,
            color = color
        )
        else -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            color = color
        )
    }
}

/**
 * Returns date styling harmonious with the selected clock font.
 */
private fun getDateTextStyle(fontStyle: String, fontSize: TextUnit, color: Color): TextStyle {
    return when (fontStyle) {
        ClockSettings.FONT_SERIF -> TextStyle(
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = fontSize,
            letterSpacing = 1.sp,
            color = color
        )
        ClockSettings.FONT_DIGITAL,
        ClockSettings.FONT_RETRO,
        ClockSettings.FONT_MONOSPACE,
        ClockSettings.FONT_SEVEN_SEGMENT -> TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = 1.5.sp,
            color = color
        )
        ClockSettings.FONT_FUTURISTIC -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            letterSpacing = 3.sp,
            color = color
        )
        else -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = 0.5.sp,
            color = color
        )
    }
}

/**
 * Computes responsive font sizes based on chosen size, full screen flag, and preview scaling.
 */
private fun getFontSizes(
    sizeSetting: String,
    isFullScreen: Boolean,
    previewScale: Float
): Pair<TextUnit, TextUnit> {
    val baseTimeSize = when (sizeSetting) {
        ClockSettings.SIZE_SMALL -> if (isFullScreen) 52f else 38f
        ClockSettings.SIZE_MEDIUM -> if (isFullScreen) 68f else 48f
        ClockSettings.SIZE_LARGE -> if (isFullScreen) 84f else 58f
        ClockSettings.SIZE_XLARGE -> if (isFullScreen) 100f else 68f
        else -> 58f
    } * previewScale

    val baseDateSize = (baseTimeSize * 0.28f).coerceIn(12f, 26f)

    return Pair(baseTimeSize.sp, baseDateSize.sp)
}

/**
 * Maps font styles to harmonious typography for the custom note badge.
 */
fun getNoteTextStyle(fontStyle: String, fontSize: TextUnit, color: Color): TextStyle {
    return when (fontStyle) {
        ClockSettings.FONT_DIGITAL -> TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize,
            letterSpacing = 1.sp,
            color = color
        )
        ClockSettings.FONT_SERIF -> TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            fontSize = fontSize,
            letterSpacing = 0.5.sp,
            color = color
        )
        ClockSettings.FONT_ROUNDED -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = fontSize,
            letterSpacing = 0.5.sp,
            color = color
        )
        ClockSettings.FONT_BOLD, ClockSettings.FONT_RETRO -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            letterSpacing = 1.sp,
            color = color
        )
        ClockSettings.FONT_FUTURISTIC, ClockSettings.FONT_SEVEN_SEGMENT -> TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            letterSpacing = 1.5.sp,
            color = color
        )
        else -> TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = fontSize,
            letterSpacing = 0.5.sp,
            color = color
        )
    }
}

