package com.example.ui.components

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ClockSettings
import com.example.data.DateRangePreset
import com.example.data.DateRangeSelection
import com.example.data.ReadingSession
import com.example.data.computeRangeAnalytics
import com.example.data.createDateRangeSelection
import com.example.util.ReadingPdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * ExportPdfDialog
 *
 * Allows the user to select any date range (up to 3 months of saved history)
 * and directly download the reading history and analysis in a beautifully designed PDF format.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportPdfDialog(
    sessions: List<ReadingSession>,
    settings: ClockSettings,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedPreset by remember { mutableStateOf(DateRangePreset.LAST_30_DAYS) }
    var customStartMillis by remember {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        mutableStateOf(cal.timeInMillis)
    }
    var customEndMillis by remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        mutableStateOf(cal.timeInMillis)
    }

    val currentSelection = remember(selectedPreset, customStartMillis, customEndMillis) {
        createDateRangeSelection(
            preset = selectedPreset,
            customStartMillis = customStartMillis,
            customEndMillis = customEndMillis
        )
    }

    val rangeAnalytics = remember(sessions, currentSelection) {
        computeRangeAnalytics(sessions, currentSelection)
    }

    var isGeneratingPdf by remember { mutableStateOf(false) }
    var generatedFile by remember { mutableStateOf<File?>(null) }
    var downloadedUri by remember { mutableStateOf<Uri?>(null) }
    var downloadFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // SAF Create Document launcher for custom folder save
    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null && generatedFile != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val success = ReadingPdfExporter.writePdfToUri(context, generatedFile!!, uri)
                withContext(Dispatchers.Main) {
                    if (success) {
                        downloadedUri = uri
                        downloadFeedbackMessage = "Saved directly to your selected location"
                        Toast.makeText(context, "PDF saved successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to write file to selected location", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun handleDirectDownload() {
        isGeneratingPdf = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // 1. Generate PDF
                val file = ReadingPdfExporter.generatePdfReport(context, rangeAnalytics)
                generatedFile = file

                // 2. Save directly to public Downloads
                val dateLabel = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val fileName = "Reading_Report_${dateLabel}_${currentSelection.preset.shortLabel.replace(" ", "_")}.pdf"
                val uri = ReadingPdfExporter.saveToDownloads(context, file, fileName)
                downloadedUri = uri

                withContext(Dispatchers.Main) {
                    isGeneratingPdf = false
                    downloadFeedbackMessage = "Downloaded to device Downloads folder ($fileName)"
                    Toast.makeText(context, "PDF downloaded successfully!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isGeneratingPdf = false
                    Toast.makeText(context, "Error creating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color(settings.colorHex).copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
            color = Color(0xFF191924),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(settings.colorHex).copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(settings.colorHex),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export Reading Report",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PDF Format",
                                color = Color(settings.colorHex),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Month Retention Show Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF222234),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(settings.colorHex).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(settings.colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Saved History: Up to 3 Months",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "History and analysis saved up to 3 months (90 days). Select any date range to download.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Select Date Range
                Text(
                    text = "Select Date Range",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateRangePreset.values().forEach { preset ->
                        val isSelected = (selectedPreset == preset)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(settings.colorHex) else Color(0xFF242436),
                            modifier = Modifier
                                .clickable { selectedPreset = preset }
                                .testTag("date_preset_${preset.name}")
                        ) {
                            Text(
                                text = preset.label,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Custom Date Range Pickers if selected
                AnimatedVisibility(visible = selectedPreset == DateRangePreset.CUSTOM) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(Color(0xFF222234), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Custom Range (within 3 months):",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Start Date Button
                            OutlinedButton(
                                onClick = {
                                    showDatePicker(context, customStartMillis) { newMillis ->
                                        customStartMillis = newMillis
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(settings.colorHex)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "From: ${dateFormat.format(Date(customStartMillis))}",
                                    fontSize = 11.sp
                                )
                            }

                            // End Date Button
                            OutlinedButton(
                                onClick = {
                                    showDatePicker(context, customEndMillis) { newMillis ->
                                        customEndMillis = newMillis
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(settings.colorHex)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "To: ${dateFormat.format(Date(customEndMillis))}",
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Card of Selected Range
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1E2C),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Preview Summary",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = currentSelection.formattedLabel,
                                fontSize = 11.sp,
                                color = Color(settings.colorHex),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PreviewMetric(
                                label = "Reading Time",
                                value = formatReadingDuration(rangeAnalytics.totalReadingSeconds),
                                icon = Icons.Default.AutoStories,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.weight(1f)
                            )
                            PreviewMetric(
                                label = "Break Time",
                                value = formatReadingDuration(rangeAnalytics.totalBreakSeconds),
                                icon = Icons.Default.FreeBreakfast,
                                tint = Color(0xFFFFB74D),
                                modifier = Modifier.weight(1f)
                            )
                            PreviewMetric(
                                label = "Sessions",
                                value = "${rangeAnalytics.totalSessions}",
                                icon = Icons.Default.Timer,
                                tint = Color(settings.colorHex),
                                modifier = Modifier.weight(0.9f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${rangeAnalytics.activeDaysCount} active reading day${if (rangeAnalytics.activeDaysCount != 1) "s" else ""} • Avg ${formatReadingDuration(rangeAnalytics.dailyAverageReadingSeconds)} / day",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Download Feedback / Success Area
                AnimatedVisibility(visible = downloadFeedbackMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .background(Color(0xFF1B382B), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = downloadFeedbackMessage ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            generatedFile?.let { pdfFile ->
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val viewIntent = ReadingPdfExporter.createViewPdfIntent(context, pdfFile)
                                            context.startActivity(viewIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No PDF viewer app found.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81C784))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open PDF", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val shareIntent = ReadingPdfExporter.createSharePdfIntent(context, pdfFile)
                                            context.startActivity(Intent.createChooser(shareIntent, "Share Reading Report"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot share PDF.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Download PDF directly
                Button(
                    onClick = { handleDirectDownload() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("direct_download_pdf_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(settings.colorHex),
                        contentColor = Color.Black
                    ),
                    enabled = !isGeneratingPdf
                ) {
                    if (isGeneratingPdf) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Generating Perfect PDF...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download PDF Report",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary "Save As..." button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                val file = generatedFile ?: ReadingPdfExporter.generatePdfReport(context, rangeAnalytics)
                                generatedFile = file
                                withContext(Dispatchers.Main) {
                                    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                                    val suggestedName = "Reading_Report_${dateStr}.pdf"
                                    createDocLauncher.launch(suggestedName)
                                }
                            }
                        },
                        enabled = !isGeneratingPdf
                    ) {
                        Text(
                            text = "Choose custom save location (Save As...)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewMetric(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF242436),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun showDatePicker(
    context: Context,
    initialMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val resultCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(resultCal.timeInMillis)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    // Bound to 3 months ago up to today
    val minCal = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }
    dialog.datePicker.minDate = minCal.timeInMillis
    dialog.datePicker.maxDate = System.currentTimeMillis()
    dialog.show()
}
