package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DayReadingStat

/**
 * WeeklyReadingChart
 *
 * Visual 7-day bar chart showing reading totals for each day of the week.
 * Highly responsive, interactive, and styled according to the active theme accent color.
 */
@Composable
fun WeeklyReadingChart(
    weeklyStats: List<DayReadingStat>,
    maxDaySeconds: Long,
    accentColorHex: Long,
    selectedDateKey: String?,
    onSelectDay: (DayReadingStat) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(accentColorHex)
    val selectedStat = weeklyStats.firstOrNull { it.dateKey == selectedDateKey }
        ?: weeklyStats.firstOrNull { it.isToday }
        ?: weeklyStats.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_reading_chart_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1B27)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.18f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Weekly Activity Analysis",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Last 7 days breakdown",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "Tap bar to inspect",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 7-Day Bars Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyStats.forEach { stat ->
                    val isSelected = (stat.dateKey == selectedStat?.dateKey)
                    DayBarItem(
                        stat = stat,
                        maxDaySeconds = maxDaySeconds,
                        accentColor = accentColor,
                        isSelected = isSelected,
                        onClick = { onSelectDay(stat) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inspection details card for selected day
            selectedStat?.let { stat ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, if (stat.isToday) accentColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stat.displayLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (stat.isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = accentColor.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = accentColor,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = buildString {
                                    append("${stat.dateSubtitle} • ${stat.sessionCount} session${if (stat.sessionCount != 1) "s" else ""}")
                                    if (stat.totalBreakSeconds > 0L) {
                                        append(" • Break: ${formatReadingDuration(stat.totalBreakSeconds)}")
                                    }
                                },
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }

                        Text(
                            text = formatReadingDuration(stat.totalSeconds),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (stat.totalSeconds > 0) accentColor else Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBarItem(
    stat: DayReadingStat,
    maxDaySeconds: Long,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heightRatio = if (maxDaySeconds > 0L) {
        (stat.totalSeconds.toFloat() / maxDaySeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val targetBarHeight = if (stat.totalSeconds > 0L) {
        (16.dp + (84.dp * heightRatio))
    } else {
        6.dp
    }
    val animatedBarHeight by animateDpAsState(
        targetValue = targetBarHeight,
        animationSpec = tween(400),
        label = "BarHeight"
    )

    val barColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor
            stat.isToday -> accentColor.copy(alpha = 0.85f)
            stat.totalSeconds > 0L -> accentColor.copy(alpha = 0.45f)
            else -> Color.White.copy(alpha = 0.12f)
        },
        label = "BarColor"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Value on top of bar
        Text(
            text = formatBriefDuration(stat.totalSeconds),
            fontSize = 9.sp,
            fontWeight = if (isSelected || stat.isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (stat.totalSeconds > 0) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bar container
        Box(
            modifier = Modifier
                .width(if (isSelected) 18.dp else 14.dp)
                .height(animatedBarHeight)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (stat.totalSeconds > 0) {
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                barColor
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(barColor, barColor)
                        )
                    }
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .padding(top = 2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day label (Mon, Tue, etc.)
        Text(
            text = stat.dayName,
            fontSize = 11.sp,
            fontWeight = if (stat.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = when {
                stat.isToday -> accentColor
                isSelected -> Color.White
                else -> Color.White.copy(alpha = 0.5f)
            }
        )

        // Today indicator dot
        if (stat.isToday) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Compact duration formatting for chart bars (e.g. "45m", "1.2h", "30s", "0")
 */
private fun formatBriefDuration(seconds: Long): String {
    if (seconds == 0L) return "0"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}
