package com.example.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BREAK_HEALTH_TIPS
import com.example.data.ReadingBreakState
import kotlinx.coroutines.delay

/**
 * ReadingBreakOverlay
 *
 * Immersive, calming full-screen overlay active during a reading break (5m, 10m, or 15m).
 *
 * Features:
 * - Real-time countdown timer (MM:SS) with circular progress ring
 * - Soft breathing guide animation encouraging relaxation and eye rest
 * - Rotating real-time eye-health and posture tips
 * - Quick controls: Resume Reading, Add +1 Min, Exit session
 * - Clear celebration and ready state when the break countdown completes
 */
@SuppressLint("DefaultLocale")
@Composable
fun ReadingBreakOverlay(
    breakState: ReadingBreakState,
    accentColorHex: Long,
    onResumeReading: () -> Unit,
    onAddOneMinute: () -> Unit,
    onExitSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(accentColorHex)

    // Breathing guide pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Rotating health tips every 7 seconds
    var tipIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(7000)
            tipIndex = (tipIndex + 1) % BREAK_HEALTH_TIPS.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C1017),
                        Color(0xFF0F1722),
                        Color(0xFF070B10)
                    )
                )
            )
            .testTag("reading_break_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background aura
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(breathScale)
                .alpha(glowAlpha)
                .background(accentColor.copy(alpha = 0.25f), CircleShape)
        )

        // Top Header Bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${breakState.selectedDurationMinutes}-Minute Reading Break",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = onExitSession,
                modifier = Modifier.testTag("break_exit_session_btn")
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Reading Session",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Center Content Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (breakState.isCompleted) {
                // Completed Break State
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Break Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Break Complete!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your eyes and mind are rested. Ready to dive back into your reading?",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onResumeReading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                        .testTag("break_completed_resume_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue Reading",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            } else {
                // Active Countdown Circle
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Circular Progress Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val arcSize = Size(diameter, diameter)

                        // Track background circle
                        drawArc(
                            color = Color.White.copy(alpha = 0.08f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Active progress arc
                        val sweep = breakState.progress * 360f
                        drawArc(
                            color = accentColor,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inner Timer Column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = breakState.formattedRemaining,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "REST YOUR EYES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (breathScale > 1.0f) "Inhale slowly..." else "Exhale gently...",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rotating Eye Health & Rest Tip Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable {
                            tipIndex = (tipIndex + 1) % BREAK_HEALTH_TIPS.size
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF161C26).copy(alpha = 0.85f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Crossfade(
                            targetState = BREAK_HEALTH_TIPS[tipIndex],
                            label = "tip_crossfade",
                            modifier = Modifier.weight(1f)
                        ) { tip ->
                            Text(
                                text = tip,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Controls Row: [ +1 Min ] and [ Resume Reading ]
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add 1 min button
                    OutlinedButton(
                        onClick = onAddOneMinute,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("break_add_one_minute_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+1 Min",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Resume reading button
                    Button(
                        onClick = onResumeReading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("break_resume_reading_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Resume Reading",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
