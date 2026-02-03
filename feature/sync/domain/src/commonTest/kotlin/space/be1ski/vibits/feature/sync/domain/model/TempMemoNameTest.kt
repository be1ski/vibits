package space.be1ski.vibits.feature.sync.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TempMemoNameTest {
  @Test
  fun `when generating temp name then starts with local_ prefix`() {
    val name = TempMemoName.generate()
    assertTrue(name.startsWith("local_"))
  }

  @Test
  fun `when generating temp name then is unique`() {
    val name1 = TempMemoName.generate()
    val name2 = TempMemoName.generate()
    assertNotEquals(name1, name2)
  }

  @Test
  fun `when checking temp name then returns true for local_ prefix`() {
    assertTrue(TempMemoName.isTemporary("local_123456789_12345"))
    assertTrue(TempMemoName.isTemporary("local_something"))
    assertTrue(TempMemoName.isTemporary("local_"))
  }

  @Test
  fun `when checking temp name then returns false for server names`() {
    assertFalse(TempMemoName.isTemporary("memos/1"))
    assertFalse(TempMemoName.isTemporary("memos/abc123"))
    assertFalse(TempMemoName.isTemporary(""))
    assertFalse(TempMemoName.isTemporary("LOCAL_123"))
  }

  @Test
  fun `when generating multiple names then all are unique`() {
    val names = (1..100).map { TempMemoName.generate() }.toSet()
    // All generated names should be unique
    assertTrue(names.size == 100)
  }

  @Test
  fun `when generated name then matches expected format`() {
    val name = TempMemoName.generate()
    // Format: local_{timestamp}_{random}
    val parts = name.removePrefix("local_").split("_")
    assertTrue(parts.size == 2)
    // First part is timestamp (long number)
    assertTrue(parts[0].all { it.isDigit() })
    // Second part is random (9 digit number)
    assertTrue(parts[1].length == 9)
    assertTrue(parts[1].all { it.isDigit() })
  }
}
