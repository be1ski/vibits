package space.be1ski.vibits.shared.core.logging

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

    assertTrue(exported.contains("D/"))
    assertTrue(exported.contains("TestTag"))
    assertTrue(exported.contains("Test message"))
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
}
