package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BreakSuggestion
import kotlinx.coroutines.delay

/**
 * BreakPromptBanner
 *
 * Real-time non-intrusive floating suggestion banner that appears in full-screen reading mode
 * when the user hits a reading time milestone (e.g. 20m, 40m, 60m).
 */
@Composable
fun BreakPromptBanner(
    suggestion: BreakSuggestion,
    accentColorHex: Long,
    onAcceptSuggestedBreak: (Int) -> Unit,
    onOpenOptions: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(accentColorHex)

    // Automatically dismiss after 20 seconds if user is deeply immersed in reading
    LaunchedEffect(suggestion) {
        delay(20000)
        onDismiss()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("realtime_break_prompt_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141923).copy(alpha = 0.96f)
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Real-Time Break Suggested",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = suggestion.reason,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 15.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("dismiss_break_prompt_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Prompt",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: [ Take Xm Break ] and [ Options (5m, 10m, 15m) ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAcceptSuggestedBreak(suggestion.durationMinutes) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("accept_suggested_break_btn")
                ) {
                    Text(
                        text = "Take ${suggestion.durationMinutes} Min Break",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                OutlinedButton(
                    onClick = onOpenOptions,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("open_break_options_from_prompt_btn")
                ) {
                    Text(
                        text = "More Options",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
