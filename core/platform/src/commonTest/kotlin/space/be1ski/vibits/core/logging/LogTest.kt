package space.be1ski.vibits.core.logging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogTest {
  @AfterTest
  fun cleanup() {
    Log.clear()
  }

  @Test
  fun `when d called then creates DEBUG entry`() {
    Log.d("TestTag", "Debug message")

    val logs = Log.logs
    assertEquals(1, logs.size)
    assertEquals(LogLevel.DEBUG, logs.first().level)
    assertEquals("TestTag", logs.first().tag)
    assertEquals("Debug message", logs.first().message)
  }

  @Test
  fun `when i called then creates INFO entry`() {
    Log.i("TestTag", "Info message")

    val logs = Log.logs
    assertEquals(1, logs.size)
    assertEquals(LogLevel.INFO, logs.first().level)
    assertEquals("TestTag", logs.first().tag)
    assertEquals("Info message", logs.first().message)
  }

  @Test
  fun `when w called then creates WARN entry`() {
    Log.w("TestTag", "Warning message")

    val logs = Log.logs
    assertEquals(1, logs.size)
    assertEquals(LogLevel.WARN, logs.first().level)
    assertEquals("TestTag", logs.first().tag)
    assertEquals("Warning message", logs.first().message)
  }

  @Test
  fun `when e called then creates ERROR entry`() {
    Log.e("TestTag", "Error message")

    val logs = Log.logs
    assertEquals(1, logs.size)
    assertEquals(LogLevel.ERROR, logs.first().level)
    assertEquals("TestTag", logs.first().tag)
    assertEquals("Error message", logs.first().message)
  }

  @Test
  fun `when e called with throwable then includes exception info`() {
    val exception = RuntimeException("Something went wrong")
    Log.e("TestTag", "Error occurred", exception)

    val logs = Log.logs
    assertEquals(1, logs.size)
    assertTrue(logs.first().message.contains("RuntimeException"))
    assertTrue(logs.first().message.contains("Something went wrong"))
  }

  @Test
  fun `when clear called then removes all logs`() {
    Log.d("Tag1", "Message 1")
    Log.i("Tag2", "Message 2")
    Log.w("Tag3", "Message 3")

    Log.clear()

    assertTrue(Log.logs.isEmpty())
  }

  @Test
  fun `when export called then formats logs correctly`() {
    Log.d("TestTag", "Test message")

    val exported = Log.export()

    assertTrue(exported.contains("D/TestTag: Test message"))
  }

  @Test
  fun `when export called then timestamp is formatted without T separator`() {
    Log.d("Tag", "Message")

    val exported = Log.export()

    // Timestamp should be "YYYY-MM-DD HH:MM:SS.mmm" format (no T, no microseconds)
    assertTrue(exported.matches(Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3} D/Tag: Message""")))
  }

  @Test
  fun `when multiple logs added then newest is first`() {
    Log.d("Tag", "First")
    Log.d("Tag", "Second")
    Log.d("Tag", "Third")

    val logs = Log.logs
    assertEquals("Third", logs[0].message)
    assertEquals("Second", logs[1].message)
    assertEquals("First", logs[2].message)
  }

  @Test
  fun `when logs property accessed then returns copy`() {
    Log.d("Tag", "Message")

    val logs1 = Log.logs
    val logs2 = Log.logs

    assertEquals(logs1, logs2)
  }

  @Test
  fun `when timestamp recorded then is not empty`() {
    Log.d("Tag", "Message")

    val entry = Log.logs.first()
    assertTrue(entry.timestamp.isNotEmpty())
  }

  @Test
  fun `when concurrent writes then no data loss`() =
    runTest {
      val count = 100
      val jobs =
        (1..count).map { i ->
          launch(Dispatchers.Default) {
            Log.d("Tag$i", "Message $i")
          }
        }
      jobs.joinAll()

      assertEquals(count, Log.logs.size)
    }

  @Test
  fun `when concurrent read and write then no crash`() =
    runTest {
      repeat(50) {
        Log.d("Initial", "Message $it")
      }

      val jobs =
        (1..100).map { i ->
          launch(Dispatchers.Default) {
            if (i % 2 == 0) {
              Log.d("Writer", "Message $i")
            } else {
              Log.logs // Read operation
            }
          }
        }
      jobs.joinAll()

      assertTrue(Log.logs.isNotEmpty())
    }

  @Test
  fun `when logs exceed max limit then oldest are removed`() {
    repeat(510) { i ->
      Log.d("Tag", "Message $i")
    }

    val logs = Log.logs
    assertTrue(logs.size <= 500)
    assertEquals("Message 509", logs.first().message)
  }

  @Test
  fun `when maskUrl called on short url then returns unchanged`() {
    val url = "https://example.com"

    val masked = url.maskUrl()

    assertEquals("https://example.com", masked)
  }

  @Test
  fun `when maskUrl called on url exceeding default length then truncates with ellipsis`() {
    val url = "https://example.com/very/long/path/that/exceeds/fifty/characters/for/sure"

    val masked = url.maskUrl()

    assertEquals("https://example.com/very/long/path/that/exceeds/fi...", masked)
  }

  @Test
  fun `when maskUrl called with custom length then respects length`() {
    val url = "https://example.com/path"

    val masked = url.maskUrl(maxLength = 10)

    assertEquals("https://ex...", masked)
  }

  @Test
  fun `when maskUrl called on empty string then returns empty`() {
    val url = ""

    val masked = url.maskUrl()

    assertEquals("", masked)
  }

  @Test
  fun `when maskUrl called on url exactly at max length then no ellipsis`() {
    val url = "a".repeat(50)

    val masked = url.maskUrl()

    assertEquals(url, masked)
  }
}
