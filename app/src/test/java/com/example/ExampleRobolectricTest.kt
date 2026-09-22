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
}
