package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.DateRangeSelection
import com.example.data.DayGroupedSessions
import com.example.data.RangeAnalytics
import com.example.data.ReadingSession
import com.example.ui.components.formatReadingDuration
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ReadingPdfExporter
 *
 * Generates an executive, beautifully formatted PDF report for reading history
 * and analytics across selected date ranges (up to 3 months / 90-day history retention).
 *
 * Design Specifications:
 * - Dimensions: Standard A4 (595 x 842 pt)
 * - Typography: Clean hierarchy with high contrast, elegant weights, and balanced whitespace
 * - Visuals: Modern dark header with indigo accent, 4 KPI metric cards, Focus-vs-Break balance bar,
 *   daily activity table, and paginated session logs with alternating row styling.
 */
object ReadingPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_X = 36f
    private const val MARGIN_BOTTOM = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

    // Palette
    private const val COLOR_HEADER_BG = 0xFF1E293B.toInt()       // Deep Slate 800
    private const val COLOR_ACCENT = 0xFF4F46E5.toInt()          // Indigo 600
    private const val COLOR_ACCENT_LIGHT = 0xFFEEF2FF.toInt()    // Indigo 50
    private const val COLOR_AMBER = 0xFFD97706.toInt()           // Amber 600
    private const val COLOR_AMBER_LIGHT = 0xFFFEF3C7.toInt()     // Amber 50
    private const val COLOR_EMERALD = 0xFF059669.toInt()         // Emerald 600
    private const val COLOR_EMERALD_LIGHT = 0xFFD1FAE5.toInt()   // Emerald 50
    private const val COLOR_TEXT_PRIMARY = 0xFF0F172A.toInt()    // Slate 900
    private const val COLOR_TEXT_MUTED = 0xFF64748B.toInt()      // Slate 500
    private const val COLOR_BORDER = 0xFFE2E8F0.toInt()          // Slate 200
    private const val COLOR_CARD_BG = 0xFFF8FAFC.toInt()         // Slate 50
    private const val COLOR_ROW_ALT = 0xFFF1F5F9.toInt()         // Slate 100

    /**
     * Builds and returns a File object pointing to the freshly generated PDF.
     */
    fun generatePdfReport(
        context: Context,
        analytics: RangeAnalytics
    ): File {
        val pdfDocument = PdfDocument()
        val exportDir = File(context.cacheDir, "pdf_reports").apply { mkdirs() }
        val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val outputFile = File(exportDir, "Reading_Report_$dateStamp.pdf")

        // Pre-plan pagination
        val pageLayouts = planPages(analytics)
        val totalPages = pageLayouts.size

        pageLayouts.forEachIndexed { pageIndex, layout ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawPage(
                canvas = canvas,
                pageIndex = pageIndex,
                totalPages = totalPages,
                layout = layout,
                analytics = analytics
            )

            pdfDocument.finishPage(page)
        }

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return outputFile
    }

    /**
     * Saves the PDF to the public Downloads folder using MediaStore (Android 10+)
     * or direct file I/O (Android 9 and below).
     */
    fun saveToDownloads(
        context: Context,
        pdfFile: File,
        customFileName: String? = null
    ): Uri? {
        val fileName = customFileName ?: pdfFile.name

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ReadingHistory")
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    pdfFile.inputStream().use { input -> input.copyTo(out) }
                }
            }
            uri
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDownloadsDir = File(downloadsDir, "ReadingHistory").apply { mkdirs() }
            val target = File(appDownloadsDir, fileName)
            pdfFile.copyTo(target, overwrite = true)
            Uri.fromFile(target)
        }
    }

    /**
     * Copies the PDF contents directly to a user-selected URI (via SAF createDocument).
     */
    fun writePdfToUri(context: Context, sourcePdf: File, destinationUri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(destinationUri)?.use { out ->
                sourcePdf.inputStream().use { input -> input.copyTo(out) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Creates an Intent to view/open the PDF in the device's default PDF viewer.
     */
    fun createViewPdfIntent(context: Context, pdfFile: File): Intent {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates an Intent to share the PDF via share sheet.
     */
    fun createSharePdfIntent(context: Context, pdfFile: File): Intent {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Reading History & Analytics Report")
            putExtra(Intent.EXTRA_TEXT, "Here is my reading history & analytics report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Rendering Engine
    // ---------------------------------------------------------------------------------------------

    private data class PageLayout(
        val isFirstPage: Boolean,
        val daySummariesSlice: List<DayGroupedSessions> = emptyList(),
        val sessionsSlice: List<ReadingSession> = emptyList(),
        val sessionStartNumber: Int = 1
    )

    private fun planPages(analytics: RangeAnalytics): List<PageLayout> {
        val pages = mutableListOf<PageLayout>()
        val allSessions = analytics.sessions
        val allDaySummaries = analytics.daySummaries

        // Page 1 budget:
        // Top Header: ~80pt
        // Date Range Banner: ~36pt
        // 4 KPI Cards: ~62pt
        // Focus vs Break Ratio Bar: ~38pt
        // Remaining space: ~480pt
        // Each daily summary row is ~22pt (plus table header 24pt)
        // Each session row is ~22pt (plus table header 24pt)

        var dayIndex = 0
        var sessionIndex = 0

        // Take up to 6 daily summaries and remaining space for sessions on Page 1
        val firstPageDays = allDaySummaries.take(8)
        dayIndex = firstPageDays.size

        // Calculate available space on page 1 for sessions
        val spaceUsedByDays = if (firstPageDays.isNotEmpty()) 30 + (firstPageDays.size * 22) else 0
        val remainingSpaceP1 = 480 - spaceUsedByDays
        val maxSessionsP1 = (remainingSpaceP1 / 22).coerceAtLeast(0)

        val firstPageSessions = allSessions.take(maxSessionsP1)
        sessionIndex = firstPageSessions.size

        pages.add(
            PageLayout(
                isFirstPage = true,
                daySummariesSlice = firstPageDays,
                sessionsSlice = firstPageSessions,
                sessionStartNumber = 1
            )
        )

        // Remaining sessions and days flow to subsequent pages
        var sessionNumberCounter = sessionIndex + 1
        while (dayIndex < allDaySummaries.size || sessionIndex < allSessions.size) {
            var pageCapacity = 680 // subsequent pages have smaller header (50pt) and footer (40pt)

            val remainingDays = if (dayIndex < allDaySummaries.size) {
                val count = minOf(allDaySummaries.size - dayIndex, pageCapacity / 22)
                val slice = allDaySummaries.subList(dayIndex, dayIndex + count)
                dayIndex += count
                pageCapacity -= (30 + count * 22)
                slice
            } else {
                emptyList()
            }

            val maxSessions = (pageCapacity / 22).coerceAtLeast(0)
            val remainingSessions = if (sessionIndex < allSessions.size && maxSessions > 0) {
                val count = minOf(allSessions.size - sessionIndex, maxSessions)
                val slice = allSessions.subList(sessionIndex, sessionIndex + count)
                sessionIndex += count
                slice
            } else {
                emptyList()
            }

            pages.add(
                PageLayout(
                    isFirstPage = false,
                    daySummariesSlice = remainingDays,
                    sessionsSlice = remainingSessions,
                    sessionStartNumber = sessionNumberCounter
                )
            )
            sessionNumberCounter += remainingSessions.size
        }

        return pages
    }

    private fun drawPage(
        canvas: Canvas,
        pageIndex: Int,
        totalPages: Int,
        layout: PageLayout,
        analytics: RangeAnalytics
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var currentY = 0f

        if (layout.isFirstPage) {
            // Full Executive Header
            currentY = drawExecutiveHeader(canvas, paint, analytics)

            // Date Range Bar
            currentY = drawDateRangeBanner(canvas, paint, analytics, currentY + 12f)

            // KPI 4-Card Grid
            currentY = drawKpiGrid(canvas, paint, analytics, currentY + 12f)

            // Focus vs Break Ratio Bar
            currentY = drawFocusRatioBar(canvas, paint, analytics, currentY + 10f)
        } else {
            // Subsequent Page Compact Header
            currentY = drawContinuationHeader(canvas, paint, analytics)
        }

        // Daily Activity Summary Table
        if (layout.daySummariesSlice.isNotEmpty()) {
            currentY = drawDailySummaryTable(
                canvas = canvas,
                paint = paint,
                summaries = layout.daySummariesSlice,
                startY = currentY + 14f,
                isFirstChunk = layout.isFirstPage
            )
        }

        // Detailed Session Logs Table
        if (layout.sessionsSlice.isNotEmpty()) {
            currentY = drawSessionsTable(
                canvas = canvas,
                paint = paint,
                sessions = layout.sessionsSlice,
                startNumber = layout.sessionStartNumber,
                startY = currentY + 14f,
                isFirstChunk = layout.isFirstPage && layout.daySummariesSlice.isEmpty()
            )
        } else if (layout.isFirstPage && analytics.sessions.isEmpty()) {
            drawEmptyNotice(canvas, paint, currentY + 30f)
        }

        // Footer on all pages
        drawFooter(canvas, paint, pageIndex + 1, totalPages)
    }

    private fun drawExecutiveHeader(canvas: Canvas, paint: Paint, analytics: RangeAnalytics): Float {
        val headerHeight = 76f

        // Header Background
        paint.color = COLOR_HEADER_BG
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), headerHeight, paint)

        // Top Accent Stripe
        paint.color = COLOR_ACCENT
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 4f, paint)

        // Title Branding
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.WHITE
        paint.textSize = 16f
        canvas.drawText("Reading History & Analytics Report", MARGIN_X, 34f, paint)

        // Subtitle / Brand Tag
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#94A3B8")
        canvas.drawText("DIGITAL CLOCK • READING FOCUS COMPANION", MARGIN_X, 48f, paint)

        // Retention Policy Tag
        paint.textSize = 8f
        paint.color = Color.parseColor("#CBD5E1")
        canvas.drawText("Retention: Up to 3 Months (90 Days)", MARGIN_X, 61f, paint)

        // Right side badge
        val dateGenerated = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date())
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#E2E8F0")
        val generatedText = "Generated: $dateGenerated"
        val textWidth = paint.measureText(generatedText)
        canvas.drawText(generatedText, PAGE_WIDTH - MARGIN_X - textWidth, 34f, paint)

        // Status Badge Pill on Right
        val badgeText = "${analytics.totalSessions} Sessions Recorded"
        paint.textSize = 8f
        val badgeW = paint.measureText(badgeText) + 16f
        val badgeRect = RectF(PAGE_WIDTH - MARGIN_X - badgeW, 46f, PAGE_WIDTH - MARGIN_X, 64f)
        paint.color = COLOR_ACCENT
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(badgeText, badgeRect.left + 8f, 58f, paint)

        return headerHeight
    }

    private fun drawContinuationHeader(canvas: Canvas, paint: Paint, analytics: RangeAnalytics): Float {
        val headerH = 44f
        paint.color = COLOR_HEADER_BG
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), headerH, paint)

        paint.color = COLOR_ACCENT
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 3f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = Color.WHITE
        paint.textSize = 12f
        canvas.drawText("Reading History Report (Continued)", MARGIN_X, 26f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#94A3B8")
        val label = analytics.dateRange.formattedLabel
        canvas.drawText("Period: $label", MARGIN_X, 38f, paint)

        return headerH
    }

    private fun drawDateRangeBanner(canvas: Canvas, paint: Paint, analytics: RangeAnalytics, startY: Float): Float {
        val bannerH = 28f
        val rect = RectF(MARGIN_X, startY, PAGE_WIDTH - MARGIN_X, startY + bannerH)

        // Background
        paint.color = COLOR_ACCENT_LIGHT
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        // Border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.parseColor("#C7D2FE")
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Text
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = COLOR_ACCENT
        paint.textSize = 9.5f
        canvas.drawText("DATE RANGE:", MARGIN_X + 10f, startY + 18f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.color = COLOR_TEXT_PRIMARY
        val rangeLabel = "${analytics.dateRange.formattedLabel} (${analytics.dateRange.preset.label})"
        canvas.drawText(rangeLabel, MARGIN_X + 78f, startY + 18f, paint)

        // Right side active days indicator
        val daysText = "${analytics.activeDaysCount} Active Days • ${analytics.dateRange.daysCount} Days in Range"
        paint.textSize = 8.5f
        paint.color = COLOR_TEXT_MUTED
        val rightW = paint.measureText(daysText)
        canvas.drawText(daysText, PAGE_WIDTH - MARGIN_X - 10f - rightW, startY + 18f, paint)

        return startY + bannerH
    }

    private fun drawKpiGrid(canvas: Canvas, paint: Paint, analytics: RangeAnalytics, startY: Float): Float {
        val cardH = 54f
        val gap = 8f
        val cardW = (CONTENT_WIDTH - (gap * 3)) / 4f

        val kpis = listOf(
            Triple("TOTAL READING", formatReadingDuration(analytics.totalReadingSeconds), COLOR_EMERALD),
            Triple("TOTAL BREAKS", formatReadingDuration(analytics.totalBreakSeconds), COLOR_AMBER),
            Triple("TOTAL SESSIONS", "${analytics.totalSessions}", COLOR_ACCENT),
            Triple("DAILY AVERAGE", "${formatReadingDuration(analytics.dailyAverageReadingSeconds)}/day", COLOR_TEXT_PRIMARY)
        )

        kpis.forEachIndexed { i, (title, value, color) ->
            val left = MARGIN_X + (i * (cardW + gap))
            val rect = RectF(left, startY, left + cardW, startY + cardH)

            // Card background
            paint.color = COLOR_CARD_BG
            canvas.drawRoundRect(rect, 6f, 6f, paint)

            // Border
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = COLOR_BORDER
            canvas.drawRoundRect(rect, 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Top accent indicator dot / stripe
            paint.color = color
            canvas.drawRoundRect(RectF(left, startY, left + cardW, startY + 3f), 3f, 3f, paint)

            // Metric label
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 7.5f
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText(title, left + 8f, startY + 17f, paint)

            // Big Metric value
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 13f
            paint.color = color
            canvas.drawText(value, left + 8f, startY + 36f, paint)

            // Small subtitle
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 7f
            paint.color = COLOR_TEXT_MUTED
            val sub = when (i) {
                0 -> "Net focus time"
                1 -> "Eye & rest breaks"
                2 -> "Saved sessions"
                else -> "Across date range"
            }
            canvas.drawText(sub, left + 8f, startY + 48f, paint)
        }

        return startY + cardH
    }

    private fun drawFocusRatioBar(canvas: Canvas, paint: Paint, analytics: RangeAnalytics, startY: Float): Float {
        val barH = 34f
        val rect = RectF(MARGIN_X, startY, PAGE_WIDTH - MARGIN_X, startY + barH)

        // Card bg
        paint.color = COLOR_CARD_BG
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = COLOR_BORDER
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        // Label
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 8f
        paint.color = COLOR_TEXT_PRIMARY
        canvas.drawText("FOCUS VS. BREAK BALANCE", MARGIN_X + 10f, startY + 13f, paint)

        val totalTime = analytics.totalReadingSeconds + analytics.totalBreakSeconds
        val focusPercent = if (totalTime > 0) ((analytics.totalReadingSeconds.toDouble() / totalTime) * 100).toInt() else 100
        val breakPercent = 100 - focusPercent

        // Right label
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8f
        paint.color = COLOR_TEXT_MUTED
        val ratioText = "$focusPercent% Focus Read • $breakPercent% Rest Breaks"
        val rWidth = paint.measureText(ratioText)
        canvas.drawText(ratioText, PAGE_WIDTH - MARGIN_X - 10f - rWidth, startY + 13f, paint)

        // Progress bar container
        val progressTop = startY + 19f
        val progressBottom = startY + 27f
        val progressRect = RectF(MARGIN_X + 10f, progressTop, PAGE_WIDTH - MARGIN_X - 10f, progressBottom)
        val progressW = progressRect.width()

        // Background / break part
        paint.color = COLOR_AMBER
        canvas.drawRoundRect(progressRect, 4f, 4f, paint)

        // Focus portion
        val focusW = (progressW * (focusPercent / 100f)).coerceIn(0f, progressW)
        if (focusW > 0) {
            val focusRect = RectF(progressRect.left, progressTop, progressRect.left + focusW, progressBottom)
            paint.color = COLOR_ACCENT
            canvas.drawRoundRect(focusRect, 4f, 4f, paint)
        }

        return startY + barH
    }

    private fun drawDailySummaryTable(
        canvas: Canvas,
        paint: Paint,
        summaries: List<DayGroupedSessions>,
        startY: Float,
        isFirstChunk: Boolean
    ): Float {
        var y = startY

        // Section Title
        if (isFirstChunk) {
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textSize = 10.5f
            paint.color = COLOR_TEXT_PRIMARY
            canvas.drawText("Daily Reading Activity Summary", MARGIN_X, y, paint)
            y += 12f
        }

        // Table Header
        val headerH = 18f
        val headerRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + headerH)
        paint.color = COLOR_ROW_ALT
        canvas.drawRoundRect(headerRect, 4f, 4f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 7.5f
        paint.color = COLOR_TEXT_MUTED

        val colDate = MARGIN_X + 8f
        val colSessions = MARGIN_X + 170f
        val colReading = MARGIN_X + 260f
        val colBreaks = MARGIN_X + 370f
        val colAvg = MARGIN_X + 460f

        canvas.drawText("DATE", colDate, y + 12f, paint)
        canvas.drawText("SESSIONS", colSessions, y + 12f, paint)
        canvas.drawText("READING TIME", colReading, y + 12f, paint)
        canvas.drawText("BREAKS", colBreaks, y + 12f, paint)
        canvas.drawText("AVG / SESSION", colAvg, y + 12f, paint)
        y += headerH + 3f

        // Table Rows
        val rowH = 18f
        summaries.forEachIndexed { index, day ->
            val rowRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowH)
            if (index % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(rowRect, paint)
            }

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 8f
            paint.color = COLOR_TEXT_PRIMARY

            canvas.drawText(day.dateLabel, colDate, y + 12f, paint)
            canvas.drawText("${day.sessionCount}", colSessions, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_EMERALD
            canvas.drawText(formatReadingDuration(day.totalSeconds), colReading, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.color = if (day.totalBreakSeconds > 0) COLOR_AMBER else COLOR_TEXT_MUTED
            canvas.drawText(
                if (day.totalBreakSeconds > 0) formatReadingDuration(day.totalBreakSeconds) else "—",
                colBreaks,
                y + 12f,
                paint
            )

            paint.color = COLOR_TEXT_MUTED
            val avg = if (day.sessionCount > 0) day.totalSeconds / day.sessionCount else 0L
            canvas.drawText(formatReadingDuration(avg), colAvg, y + 12f, paint)

            // Row bottom line
            paint.color = COLOR_BORDER
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN_X, y + rowH, PAGE_WIDTH - MARGIN_X, y + rowH, paint)

            y += rowH
        }

        return y
    }

    private fun drawSessionsTable(
        canvas: Canvas,
        paint: Paint,
        sessions: List<ReadingSession>,
        startNumber: Int,
        startY: Float,
        isFirstChunk: Boolean
    ): Float {
        var y = startY

        // Section Title
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10.5f
        paint.color = COLOR_TEXT_PRIMARY
        canvas.drawText("Detailed Session Logs", MARGIN_X, y, paint)
        y += 12f

        // Table Header
        val headerH = 18f
        val headerRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + headerH)
        paint.color = COLOR_ROW_ALT
        canvas.drawRoundRect(headerRect, 4f, 4f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 7.5f
        paint.color = COLOR_TEXT_MUTED

        val colNum = MARGIN_X + 6f
        val colDate = MARGIN_X + 32f
        val colTime = MARGIN_X + 130f
        val colDuration = MARGIN_X + 230f
        val colBreak = MARGIN_X + 330f
        val colNotes = MARGIN_X + 420f

        canvas.drawText("#", colNum, y + 12f, paint)
        canvas.drawText("DATE", colDate, y + 12f, paint)
        canvas.drawText("TIME (START – END)", colTime, y + 12f, paint)
        canvas.drawText("DURATION", colDuration, y + 12f, paint)
        canvas.drawText("BREAK TAKEN", colBreak, y + 12f, paint)
        canvas.drawText("NOTES / TAG", colNotes, y + 12f, paint)
        y += headerH + 3f

        val rowH = 19f
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        sessions.forEachIndexed { idx, session ->
            val num = startNumber + idx
            val rowRect = RectF(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y + rowH)

            if (idx % 2 == 1) {
                paint.color = Color.parseColor("#F8FAFC")
                canvas.drawRect(rowRect, paint)
            }

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 8f
            paint.color = COLOR_TEXT_MUTED
            canvas.drawText("$num", colNum, y + 12f, paint)

            paint.color = COLOR_TEXT_PRIMARY
            canvas.drawText(dateFormat.format(Date(session.endTimeMillis)), colDate, y + 12f, paint)

            val timeSpan = "${timeFormat.format(Date(session.startTimeMillis))} – ${timeFormat.format(Date(session.endTimeMillis))}"
            canvas.drawText(timeSpan, colTime, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.color = COLOR_EMERALD
            canvas.drawText(formatReadingDuration(session.durationSeconds), colDuration, y + 12f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            if (session.breakDurationSeconds > 0) {
                paint.color = COLOR_AMBER
                canvas.drawText(formatReadingDuration(session.breakDurationSeconds), colBreak, y + 12f, paint)
            } else {
                paint.color = COLOR_TEXT_MUTED
                canvas.drawText("None", colBreak, y + 12f, paint)
            }

            paint.color = COLOR_TEXT_MUTED
            val note = if (session.note.isNotBlank()) session.note.take(24) else "—"
            canvas.drawText(note, colNotes, y + 12f, paint)

            // Row separator
            paint.color = COLOR_BORDER
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN_X, y + rowH, PAGE_WIDTH - MARGIN_X, y + rowH, paint)

            y += rowH
        }

        return y
    }

    private fun drawEmptyNotice(canvas: Canvas, paint: Paint, startY: Float) {
        val rect = RectF(MARGIN_X, startY, PAGE_WIDTH - MARGIN_X, startY + 60f)
        paint.color = COLOR_CARD_BG
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = COLOR_BORDER
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10f
        paint.color = COLOR_TEXT_PRIMARY
        canvas.drawText("No Reading Sessions in this Selected Date Range", MARGIN_X + 20f, startY + 28f, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = COLOR_TEXT_MUTED
        canvas.drawText("Try expanding your date range filter or start a new full-screen reading session.", MARGIN_X + 20f, startY + 44f, paint)
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, pageNumber: Int, totalPages: Int) {
        val y = PAGE_HEIGHT - MARGIN_BOTTOM

        // Divider
        paint.color = COLOR_BORDER
        paint.strokeWidth = 0.8f
        canvas.drawLine(MARGIN_X, y, PAGE_WIDTH - MARGIN_X, y, paint)

        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 7.5f
        paint.color = Color.parseColor("#94A3B8")

        // Left Note
        canvas.drawText("Digital Clock Reading Focus • Personal Study & Productivity Report", MARGIN_X, y + 14f, paint)

        // Center Note
        val centerText = "3-Month Maximum Saved History (90-Day Retention)"
        val centerW = paint.measureText(centerText)
        canvas.drawText(centerText, (PAGE_WIDTH - centerW) / 2f, y + 14f, paint)

        // Right Page Number
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.color = COLOR_TEXT_MUTED
        val pageText = "Page $pageNumber of $totalPages"
        val pageW = paint.measureText(pageText)
        canvas.drawText(pageText, PAGE_WIDTH - MARGIN_X - pageW, y + 14f, paint)
    }
}
