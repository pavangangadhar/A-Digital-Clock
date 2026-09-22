package com.example.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.data.ReadingSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ReadingSessionDialog
 *
 * Appears immediately upon exiting full-screen clock mode:
 * - Shows exactly how much time the user spent reading
 * - Informs the user that the session has been saved to local Room storage
 * - Provides an immediate "Delete Session" action if the user does not want to keep it
 * - Provides a "View History" action to see all past reading sessions
 */
@SuppressLint("NewApi")
@Composable
fun ReadingSessionDialog(
    session: ReadingSession,
    settings: ClockSettings,
    todayTotalSeconds: Long = session.durationSeconds,
    weekTotalSeconds: Long = session.durationSeconds,
    onDismiss: () -> Unit,
    onDelete: (Long) -> Unit,
    onViewHistory: () -> Unit
) {
    val durationText = formatReadingDuration(session.durationSeconds)
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(session.endTimeMillis))

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("reading_session_completion_dialog"),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color(0xFF1E1E28),
        icon = {
            Surface(
                shape = CircleShape,
                color = Color(settings.colorHex).copy(alpha = 0.2f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "Reading Completed",
                        tint = Color(settings.colorHex),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Reading Session Saved! 📖",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Great focus! Here is how much time you read:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Highlighted Duration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = durationText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(settings.colorHex),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recorded at $formattedTime",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        if (session.breakDurationSeconds > 0L) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFB74D).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FreeBreakfast,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Break Taken: ${formatReadingDuration(session.breakDurationSeconds)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFFFB74D)
                                    )
                                }
                            }
                        }
                    }
                }

                // Daily & Weekly analysis summary cards
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Today's Total",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatReadingDuration(todayTotalSeconds),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(settings.colorHex)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "This Week",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatReadingDuration(weekTotalSeconds),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(settings.colorHex)
                            )
                        }
                    }
                }

                // Optional Custom Note
                if (session.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "📝 ${session.note}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Saved in local history. You can view or delete it anytime.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dialog_keep_session_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(settings.colorHex)
                )
            ) {
                Text(
                    text = "Keep & Done",
                    fontWeight = FontWeight.Bold,
                    color = if (isLightDialogColor(settings.colorHex)) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // View History Button
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onViewHistory()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_view_history_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "History", fontSize = 12.sp)
                    }

                    // Delete This Session Button
                    OutlinedButton(
                        onClick = {
                            onDelete(session.id)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_delete_session_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF5252)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Delete", fontSize = 12.sp)
                    }
                }
            }
        }
    )
}

/**
 * Friendly duration text (e.g., "12 min 30 sec", "1 hr 15 min", "45 sec")
 */
fun formatReadingDuration(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> "${hours} hr ${minutes} min"
        minutes > 0 -> "${minutes} min ${seconds} sec"
        else -> "${seconds} sec"
    }
}

private fun isLightDialogColor(colorHex: Long): Boolean {
    val r = ((colorHex shr 16) and 0xFF) / 255.0
    val g = ((colorHex shr 8) and 0xFF) / 255.0
    val b = (colorHex and 0xFF) / 255.0
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
    return luminance > 0.5
}
