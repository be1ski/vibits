package space.be1ski.vibits.feature.sync.domain.model

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class OperationIdTest {
  @Test
  fun `when generating id then starts with op_ prefix`() {
    val id = OperationId.generate()
    assertTrue(id.startsWith("op_"))
  }

  @Test
  fun `when generating id then is unique`() {
    val id1 = OperationId.generate()
    val id2 = OperationId.generate()
    assertNotEquals(id1, id2)
  }

  @Test
  fun `when generating multiple ids then all are unique`() {
    val ids = (1..100).map { OperationId.generate() }.toSet()
    assertTrue(ids.size == 100)
  }

  @Test
  fun `when generated id then matches expected format`() {
    val id = OperationId.generate()
    // Format: op_{timestamp}_{random}
    val parts = id.removePrefix("op_").split("_")
    assertTrue(parts.size == 2)
    // First part is timestamp (long number)
    assertTrue(parts[0].all { it.isDigit() })
    // Second part is random number
    assertTrue(parts[1].all { it.isDigit() })
  }
}
