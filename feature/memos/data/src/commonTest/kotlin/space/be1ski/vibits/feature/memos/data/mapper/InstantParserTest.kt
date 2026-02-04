package space.be1ski.vibits.feature.memos.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class InstantParserTest {
  @Test
  fun `when null then returns null`() {
    assertNull(parseInstant(null))
  }

  @Test
  fun `when blank then returns null`() {
    assertNull(parseInstant(""))
    assertNull(parseInstant("   "))
  }

  @Test
  fun `when ISO timestamp then parses correctly`() {
    val result = parseInstant("2024-01-02T03:04:05Z")

    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), result)
  }

  @Test
  fun `when ISO timestamp with whitespace then trims and parses`() {
    val result = parseInstant("  2024-01-02T03:04:05Z  ")

    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), result)
  }

  @Test
  fun `when ISO timestamp without zone then adds UTC`() {
    val result = parseInstant("2024-01-02T03:04:05")

    assertEquals(Instant.parse("2024-01-02T03:04:05Z"), result)
  }

  @Test
  fun `when epoch seconds then converts to millis`() {
    val result = parseInstant("1700000000")

    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), result)
  }

  @Test
  fun `when epoch millis then keeps as millis`() {
    val result = parseInstant("1700000000000")

    assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), result)
  }

  @Test
  fun `when invalid format then returns null`() {
    assertNull(parseInstant("not-a-date"))
    assertNull(parseInstant("abc123"))
  }
}
