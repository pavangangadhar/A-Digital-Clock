package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * SevenSegmentClockView
 *
 * An educational, custom-drawn 7-segment LED/LCD digital display built entirely
 * with Jetpack Compose Canvas.
 *
 * Educational Note:
 * - Each digital number consists of 7 segments (a, b, c, d, e, f, g).
 * - Real digital clocks display inactive segments dimly (ghosting), which we simulate
 *   by drawing inactive segments with 10% opacity.
 * - This demonstrates custom 2D Canvas drawing, proportional coordinate calculation,
 *   and responsive scaling in Compose.
 */
@Composable
fun SevenSegmentClockView(
    timeString: String,
    activeColor: Color,
    digitWidth: Dp = 42.dp,
    digitHeight: Dp = 76.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        timeString.forEach { char ->
            when (char) {
                in '0'..'9' -> {
                    SevenSegmentDigit(
                        digit = char,
                        activeColor = activeColor,
                        modifier = Modifier.size(width = digitWidth, height = digitHeight)
                    )
                }
                ':' -> {
                    SevenSegmentColon(
                        activeColor = activeColor,
                        modifier = Modifier.size(width = digitWidth * 0.35f, height = digitHeight)
                    )
                }
                ' ' -> {
                    // Spacer for AM/PM separation
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.size(width = digitWidth * 0.25f, height = digitHeight)
                    )
                }
                else -> {
                    // Fallback for letters like A, P, M if in time string
                    SevenSegmentDigit(
                        digit = char,
                        activeColor = activeColor,
                        modifier = Modifier.size(width = digitWidth, height = digitHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun SevenSegmentDigit(
    digit: Char,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val inactiveColor = activeColor.copy(alpha = 0.10f)

    // Segment mapping: a=top, b=top-right, c=bottom-right, d=bottom, e=bottom-left, f=top-left, g=center
    val segments = getActiveSegments(digit)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val thickness = w * 0.16f
        val gap = w * 0.05f

        // Segment A: Top Horizontal
        drawRoundRect(
            color = if (segments[0]) activeColor else inactiveColor,
            topLeft = Offset(thickness + gap, 0f),
            size = Size(w - 2 * (thickness + gap), thickness),
            cornerRadius = CornerRadius(thickness / 2, thickness / 2)
        )

        // Segment B: Top-Right Vertical
        drawRoundRect(
            color = if (segments[1]) activeColor else inactiveColor,
            topLeft = Offset(w - thickness, thickness + gap),
            size = Size(thickness, (h / 2) - thickness - 2 * gap),
            cornerRadius = CornerRadius(thickness / 2, thickness / 2)
        )

        // Segment C: Bottom-Right Vertical
        drawRoundRect(
            color = if (segments[2]) activeColor else inactiveColor,
            topLeft = Offset(w - thickness, (h / 2) + gap),
            size = Size(thickness, (h / 2) - thickness - 2 * gap),
            cornerRadius = CornerRadius(thickness / 2, thickness / 2)
        )

        // Segment D: Bottom Horizontal
        drawRoundRect(
            color = if (segments[3]) activeColor else inactiveColor,
            topLeft = Offset(thickness + gap, h - thickness),
            size = Size(w - 2 * (thickness + gap), thickness),
            cornerRadius = CornerRadius(thickness / 2, thickness / 2)
        )

        // Segment E: Bottom-Left Vertical
        drawRoundRect(
            color = if (segments[4]) activeColor else inactiveColor,
            topLeft = Offset(0f, (h / 2) + gap),
            size = Size(thickness, (h / 2) - thickness - 2 * gap),
            cornerRadius = CornerRadius(thickness / 2, thickness / 2)
        )

        // Segment F: Top-Left Vertical
        drawRoundRect(
            color = if (segments[5]) activeColor else inactiveColor,
            topLeft = Offset(0f, thickness + gap),
            size = Size(thickness, (h / 2) - thickness - 2 * gap),
            cornerRadius = CornerRadius(thickness / 2, thickness / 2)
        )

        // Segment G: Middle Horizontal
        drawRoundRect(
            color = if (segments[6]) activeColor else inactiveColor,
            topLeft = Offset(thickness + gap, (h / 2) - (thickness / 2)),
            size = Size(w - 2 * (thickness + gap), thickness),
            cornerRadius = CornerRadius(thickness / 2, thickness / 2)
        )
    }
}

@Composable
private fun SevenSegmentColon(
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val dotRadius = w * 0.32f

        // Top dot
        drawCircle(
            color = activeColor,
            radius = dotRadius,
            center = Offset(w / 2, h * 0.35f)
        )

        // Bottom dot
        drawCircle(
            color = activeColor,
            radius = dotRadius,
            center = Offset(w / 2, h * 0.65f)
        )
    }
}

/**
 * Returns a boolean array of 7 items representing [a, b, c, d, e, f, g]
 */
private fun getActiveSegments(char: Char): BooleanArray {
    return when (char) {
        '0' -> booleanArrayOf(true, true, true, true, true, true, false)
        '1' -> booleanArrayOf(false, true, true, false, false, false, false)
        '2' -> booleanArrayOf(true, true, false, true, true, false, true)
        '3' -> booleanArrayOf(true, true, true, true, false, false, true)
        '4' -> booleanArrayOf(false, true, true, false, false, true, true)
        '5' -> booleanArrayOf(true, false, true, true, false, true, true)
        '6' -> booleanArrayOf(true, false, true, true, true, true, true)
        '7' -> booleanArrayOf(true, true, true, false, false, false, false)
        '8' -> booleanArrayOf(true, true, true, true, true, true, true)
        '9' -> booleanArrayOf(true, true, true, true, false, true, true)
        'A', 'a' -> booleanArrayOf(true, true, true, false, true, true, true)
        'P', 'p' -> booleanArrayOf(true, true, false, false, true, true, true)
        'M', 'm' -> booleanArrayOf(true, false, true, false, true, false, false)
        '-' -> booleanArrayOf(false, false, false, false, false, false, true)
        else -> booleanArrayOf(false, false, false, false, false, false, false)
    }
}
