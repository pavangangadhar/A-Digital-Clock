package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BreakSuggestion

/**
 * BreakOptionsDialog
 *
 * Full-featured modal providing:
 * - Real-time break suggestion calculated from current reading elapsed time
 * - 5 min, 10 min, and 15 min quick break start cards
 * - Clear explanation of eye-care benefits for each duration
 */
@Composable
fun BreakOptionsDialog(
    suggestion: BreakSuggestion,
    accentColorHex: Long,
    onSelectDuration: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val accentColor = Color(accentColorHex)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF191D26),
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Take a Reading Break",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Rest your eyes & refresh your mind",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Real-time Intelligent Recommendation Pill Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = accentColor.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-TIME SUGGESTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = suggestion.reason,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 16.sp
                        )
                    }
                }

                Text(
                    text = "Select break duration:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )

                // 5 MIN BREAK CARD
                BreakDurationCard(
                    minutes = 5,
                    title = "5 Minutes",
                    subtitle = "Quick Eye Rest (20-20-20 rule)",
                    description = "Look into the distance, blink & relax eye muscles.",
                    icon = Icons.Default.Visibility,
                    isSuggested = (suggestion.durationMinutes == 5),
                    accentColor = accentColor,
                    onClick = { onSelectDuration(5) },
                    testTag = "break_option_5m"
                )

                // 10 MIN BREAK CARD
                BreakDurationCard(
                    minutes = 10,
                    title = "10 Minutes",
                    subtitle = "Stretch & Hydrate",
                    description = "Stand up, drink water, stretch neck and shoulders.",
                    icon = Icons.Default.FitnessCenter,
                    isSuggested = (suggestion.durationMinutes == 10),
                    accentColor = accentColor,
                    onClick = { onSelectDuration(10) },
                    testTag = "break_option_10m"
                )

                // 15 MIN BREAK CARD
                BreakDurationCard(
                    minutes = 15,
                    title = "15 Minutes",
                    subtitle = "Deep Recharge",
                    description = "Restorative walk, disconnect, and mental refresh.",
                    icon = Icons.Default.SelfImprovement,
                    isSuggested = (suggestion.durationMinutes == 15),
                    accentColor = accentColor,
                    onClick = { onSelectDuration(15) },
                    testTag = "break_option_15m"
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_break_options_btn")
            ) {
                Text(
                    text = "Continue Reading",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

/**
 * Individual duration choice card
 */
@Composable
private fun BreakDurationCard(
    minutes: Int,
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    isSuggested: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuggested) Color(0xFF222A38) else Color(0xFF1E222C)
        ),
        border = BorderStroke(
            1.dp,
            if (isSuggested) accentColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSuggested) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSuggested) accentColor else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isSuggested) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accentColor
                        ) {
                            Text(
                                text = "RECOMMENDED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSuggested) accentColor else Color.White.copy(alpha = 0.7f)
                )

                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    lineHeight = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSuggested) accentColor else Color.White.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Start",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSuggested) Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
