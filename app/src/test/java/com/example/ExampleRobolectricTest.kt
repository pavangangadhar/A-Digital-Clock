package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ClockDatabase
import com.example.data.ReadingSession
import com.example.ui.components.formatReadingDuration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: ClockDatabase

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, ClockDatabase::class.java)
        .allowMainThreadQueries()
        .build()
  }

  @After
  fun teardown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Digital Clock", appName)
  }

  @Test
  fun `format reading duration returns friendly text`() {
    assertEquals("45 sec", formatReadingDuration(45L))
    assertEquals("2 min 30 sec", formatReadingDuration(150L))
    assertEquals("1 hr 15 min", formatReadingDuration(4500L))
  }

  @Test
  fun `insert and delete reading session in database`() = runBlocking {
    val dao = database.readingSessionDao()
    val session = ReadingSession(
        durationSeconds = 120L,
        startTimeMillis = 1000L,
        endTimeMillis = 121000L,
        note = "Study Kotlin",
        breakDurationSeconds = 300L
    )

    val id = dao.insertSession(session)
    assertTrue(id > 0)

    val sessions = dao.getAllSessions().first()
    assertEquals(1, sessions.size)
    assertEquals(120L, sessions[0].durationSeconds)
    assertEquals(300L, sessions[0].breakDurationSeconds)
    assertEquals("Study Kotlin", sessions[0].note)

    // Test delete
    dao.deleteSessionById(id)
    val emptySessions = dao.getAllSessions().first()
    assertTrue(emptySessions.isEmpty())
  }

  @Test
  fun `three month retention pruning removes old sessions and preserves recent sessions`() = runBlocking {
    val dao = database.readingSessionDao()
    val cal = java.util.Calendar.getInstance()
    val now = cal.timeInMillis

    // Session within last week (recent)
    val recentSession = ReadingSession(
        durationSeconds = 1800L,
        startTimeMillis = now - 7 * 24 * 60 * 60 * 1000L,
        endTimeMillis = now - 7 * 24 * 60 * 60 * 1000L + 1800000L
    )

    // Session 4 months ago (older than 3 months)
    cal.add(java.util.Calendar.MONTH, -4)
    val fourMonthsAgo = cal.timeInMillis
    val oldSession = ReadingSession(
        durationSeconds = 2400L,
        startTimeMillis = fourMonthsAgo,
        endTimeMillis = fourMonthsAgo + 2400000L
    )

    dao.insertSession(recentSession)
    dao.insertSession(oldSession)

    val allBefore = dao.getAllSessions().first()
    assertEquals(2, allBefore.size)

    // Calculate 3-month cutoff
    val cutoffCal = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.MONTH, -3)
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val deletedCount = dao.pruneSessionsOlderThan(cutoffCal.timeInMillis)
    assertEquals(1, deletedCount)

    val allAfter = dao.getAllSessions().first()
    assertEquals(1, allAfter.size)
    assertEquals(recentSession.durationSeconds, allAfter[0].durationSeconds)
  }

  @Test
  fun `getSessionsInRange returns only records within selected boundaries`() = runBlocking {
    val dao = database.readingSessionDao()
    val baseTime = 1700000000000L

    val s1 = ReadingSession(durationSeconds = 100L, startTimeMillis = baseTime + 1000L, endTimeMillis = baseTime + 101000L)
    val s2 = ReadingSession(durationSeconds = 200L, startTimeMillis = baseTime + 200000L, endTimeMillis = baseTime + 400000L)
    val s3 = ReadingSession(durationSeconds = 300L, startTimeMillis = baseTime + 500000L, endTimeMillis = baseTime + 800000L)

    dao.insertSession(s1)
    dao.insertSession(s2)
    dao.insertSession(s3)

    val inRange = dao.getSessionsInRange(baseTime + 150000L, baseTime + 450000L)
    assertEquals(1, inRange.size)
    assertEquals(200L, inRange[0].durationSeconds)
  }

  @Test
  fun `pdf exporter intent creation builds valid action view and action send intents`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tempFile = java.io.File(context.cacheDir, "sample_test.pdf").apply {
        writeText("%PDF-1.4 Mock PDF Content")
    }

    val viewIntent = com.example.util.ReadingPdfExporter.createViewPdfIntent(context, tempFile)
    assertEquals(android.content.Intent.ACTION_VIEW, viewIntent.action)
    assertEquals("application/pdf", viewIntent.type)
    assertTrue((viewIntent.flags and android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)

    val shareIntent = com.example.util.ReadingPdfExporter.createSharePdfIntent(context, tempFile)
    assertEquals(android.content.Intent.ACTION_SEND, shareIntent.action)
    assertEquals("application/pdf", shareIntent.type)
  }
}
