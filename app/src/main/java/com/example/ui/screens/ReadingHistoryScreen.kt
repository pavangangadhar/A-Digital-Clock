package com.example.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClockSettings
import com.example.data.DayGroupedSessions
import com.example.data.ReadingAnalytics
import com.example.data.ReadingSession
import com.example.data.computeReadingAnalytics
import com.example.ui.components.WeeklyReadingChart
import com.example.ui.components.formatReadingDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HistoryViewMode {
    ANALYSIS,
    SESSIONS
}

/**
 * ReadingHistoryScreen
 *
 * Full-featured analytics and session management screen providing:
 * 1. Daily Reading Analysis: Total read today, session count, yesterday comparison
 * 2. Weekly Reading Analysis: Total read this week, daily average, 7-day visual chart
 * 3. Individual Saved Session Management: Grouped by day with delete options
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("NewApi")
@Composable
fun ReadingHistoryScreen(
    sessions: List<ReadingSession>,
    settings: ClockSettings,
    onDeleteSession: (Long) -> Unit,
    onClearAllSessions: () -> Unit,
    onStartFullScreen: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = remember(sessions) { computeReadingAnalytics(sessions) }
    var currentViewMode by remember { mutableStateOf(HistoryViewMode.ANALYSIS) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var selectedDateKey by remember { mutableStateOf<String?>(null) }
    var sessionToDelete by remember { mutableStateOf<ReadingSession?>(null) }

    // Clear All confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(text = "Clear All Reading History?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "This will permanently delete all ${sessions.size} saved reading sessions from your local database.",
                    color = Color.White.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllSessions()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.testTag("confirm_clear_all_btn")
                ) {
                    Text(text = "Delete All", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(text = "Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF22222E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Delete single session confirmation dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(text = "Delete This Reading Session?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Duration: ${formatReadingDuration(session.durationSeconds)}\nRecorded: ${SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(session.endTimeMillis))}",
                    color = Color.White.copy(alpha = 0.75f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSession(session.id)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.testTag("confirm_delete_single_btn")
                ) {
                    Text(text = "Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text(text = "Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF22222E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Reading Analysis & History",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Today: ${formatReadingDuration(analytics.todaySeconds)} • Week: ${formatReadingDuration(analytics.weekSeconds)}",
                            color = Color(settings.colorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("history_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (sessions.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.testTag("clear_all_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear All History",
                                tint = Color(0xFFFF6B6B)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF14141E)
                )
            )
        },
        containerColor = Color(0xFF14141E),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (sessions.isEmpty()) {
            // Empty State
            EmptyHistoryView(
                accentColorHex = settings.colorHex,
                onStartFullScreen = onStartFullScreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Segmented Tab Switcher (Analysis vs Session Logs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color(0xFF1F1F2C), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SegmentTabButton(
                        title = "📊 Analysis & Insights",
                        selected = (currentViewMode == HistoryViewMode.ANALYSIS),
                        accentColorHex = settings.colorHex,
                        onClick = { currentViewMode = HistoryViewMode.ANALYSIS },
                        modifier = Modifier.weight(1f)
                    )
                    SegmentTabButton(
                        title = "📋 Sessions (${sessions.size})",
                        selected = (currentViewMode == HistoryViewMode.SESSIONS),
                        accentColorHex = settings.colorHex,
                        onClick = { currentViewMode = HistoryViewMode.SESSIONS },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tab Content with Crossfade
                Crossfade(
                    targetState = currentViewMode,
                    label = "HistoryModeTransition",
                    modifier = Modifier.fillMaxSize()
                ) { mode ->
                    when (mode) {
                        HistoryViewMode.ANALYSIS -> {
                            AnalysisTabContent(
                                analytics = analytics,
                                settings = settings,
                                selectedDateKey = selectedDateKey,
                                onSelectDay = { stat -> selectedDateKey = stat.dateKey },
                                onSwitchToSessions = { currentViewMode = HistoryViewMode.SESSIONS }
                            )
                        }
                        HistoryViewMode.SESSIONS -> {
                            SessionsTabContent(
                                dayGroups = analytics.dayGroups,
                                accentColorHex = settings.colorHex,
                                onRequestDeleteSession = { session -> sessionToDelete = session }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Segmented Tab Button
 */
@Composable
private fun SegmentTabButton(
    title: String,
    selected: Boolean,
    accentColorHex: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color(accentColorHex).copy(alpha = 0.22f) else Color.Transparent,
        label = "TabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color(accentColorHex) else Color.White.copy(alpha = 0.6f),
        label = "TabContent"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        border = if (selected) BorderStroke(1.dp, Color(accentColorHex).copy(alpha = 0.5f)) else null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Analysis Tab Content: Daily + Weekly analysis, 7-day bar chart, day-by-day stats
 */
@Composable
private fun AnalysisTabContent(
    analytics: ReadingAnalytics,
    settings: ClockSettings,
    selectedDateKey: String?,
    onSelectDay: (com.example.data.DayReadingStat) -> Unit,
    onSwitchToSessions: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analysis_tab_content"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Daily & Weekly KPI Highlight Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TODAY'S TOTAL CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("today_analysis_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2A))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(settings.colorHex).copy(alpha = 0.2f),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(settings.colorHex),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today's Read",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = formatReadingDuration(analytics.todaySeconds),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(settings.colorHex)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${analytics.todaySessionsCount} session${if (analytics.todaySessionsCount != 1) "s" else ""} today",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )

                        if (analytics.yesterdaySeconds > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Yesterday: ${formatReadingDuration(analytics.yesterdaySeconds)}",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        }
                    }
                }

                // WEEKLY TOTAL CARD
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("weekly_analysis_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2A))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "This Week",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = formatReadingDuration(analytics.weekSeconds),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF81C784)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${analytics.weekSessionsCount} session${if (analytics.weekSessionsCount != 1) "s" else ""} this week",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.55f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Avg ${formatReadingDuration(analytics.dailyAverageThisWeekSeconds)} / day",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }
                }
            }
        }

        // Weekly 7-Day Visual Interactive Bar Chart
        item {
            WeeklyReadingChart(
                weeklyStats = analytics.weeklyDayStats,
                maxDaySeconds = analytics.maxDaySecondsInWeek,
                accentColorHex = settings.colorHex,
                selectedDateKey = selectedDateKey,
                onSelectDay = onSelectDay
            )
        }

        // All-Time Summary & Insights Pill Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF171724))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cumulative Insights",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            onClick = onSwitchToSessions,
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "View All Logs →",
                                fontSize = 11.sp,
                                color = Color(settings.colorHex),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatPill(
                            label = "All-Time Total",
                            value = formatReadingDuration(analytics.allTimeSeconds),
                            icon = Icons.Default.AutoStories
                        )
                        StatPill(
                            label = "Total Sessions",
                            value = "${analytics.allTimeSessionsCount}",
                            icon = Icons.Default.Timer
                        )
                    }
                }
            }
        }

        // Day-by-day reading log cards
        item {
            Text(
                text = "Daily Breakdown",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(analytics.dayGroups, key = { it.dateKey }) { group ->
            DaySummaryCard(
                group = group,
                accentColorHex = settings.colorHex
            )
        }
    }
}

/**
 * Compact card showing daily total and number of sessions on that day
 */
@Composable
private fun DaySummaryCard(
    group: DayGroupedSessions,
    accentColorHex: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B27))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = group.dateLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${group.sessionCount} session${if (group.sessionCount != 1) "s" else ""}",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(accentColorHex).copy(alpha = 0.15f)
            ) {
                Text(
                    text = formatReadingDuration(group.totalSeconds),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(accentColorHex),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Sessions Tab Content: List of all individual sessions grouped by day with delete options
 */
@Composable
private fun SessionsTabContent(
    dayGroups: List<DayGroupedSessions>,
    accentColorHex: Long,
    onRequestDeleteSession: (ReadingSession) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sessions_tab_content"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        dayGroups.forEach { group ->
            item(key = "header_${group.dateKey}") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.dateLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(accentColorHex)
                    )
                    Text(
                        text = "Total: ${formatReadingDuration(group.totalSeconds)}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            items(group.sessions, key = { it.id }) { session ->
                SessionItemCard(
                    session = session,
                    accentColorHex = accentColorHex,
                    onDelete = { onRequestDeleteSession(session) }
                )
            }
        }
    }
}

/**
 * Individual session card with duration, time, custom note, and delete action.
 */
@Composable
private fun SessionItemCard(
    session: ReadingSession,
    accentColorHex: Long,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(session.endTimeMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_item_${session.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C28)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(accentColorHex).copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = Color(accentColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = formatReadingDuration(session.durationSeconds),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Ended at $formattedTime",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )

                    if (session.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📝 ${session.note}",
                            fontSize = 11.sp,
                            color = Color(accentColorHex).copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Delete action button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_session_${session.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Session",
                    tint = Color(0xFFFF6B6B).copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Small stat badge pill
 */
@Composable
private fun StatPill(
    label: String,
    value: String,
    icon: ImageVector
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Empty state view when no sessions have been saved yet.
 */
@Composable
private fun EmptyHistoryView(
    accentColorHex: Long,
    onStartFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(accentColorHex).copy(alpha = 0.12f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = Color(accentColorHex),
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No Reading Sessions Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter Full Screen Clock mode to start tracking your reading time. The app will automatically calculate how much you read in a day and show your weekly reading analysis here.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartFullScreen,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(accentColorHex)
            ),
            modifier = Modifier.testTag("empty_start_fullscreen_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Reading in Full Screen",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
