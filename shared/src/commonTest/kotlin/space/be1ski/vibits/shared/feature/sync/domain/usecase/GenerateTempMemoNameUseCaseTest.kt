package space.be1ski.vibits.shared.feature.sync.domain.usecase

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GenerateTempMemoNameUseCaseTest {
  @Test
  fun `when generating temp name then starts with local_ prefix`() {
    val name = GenerateTempMemoNameUseCase()
    assertTrue(name.startsWith("local_"))
  }

  @Test
  fun `when generating temp name then is unique`() {
    val name1 = GenerateTempMemoNameUseCase()
    val name2 = GenerateTempMemoNameUseCase()
    assertNotEquals(name1, name2)
  }

  @Test
  fun `when checking temp name then returns true for local_ prefix`() {
    assertTrue(GenerateTempMemoNameUseCase.isTemporaryName("local_123456789_12345"))
    assertTrue(GenerateTempMemoNameUseCase.isTemporaryName("local_something"))
    assertTrue(GenerateTempMemoNameUseCase.isTemporaryName("local_"))
  }

  @Test
  fun `when checking temp name then returns false for server names`() {
    assertFalse(GenerateTempMemoNameUseCase.isTemporaryName("memos/1"))
    assertFalse(GenerateTempMemoNameUseCase.isTemporaryName("memos/abc123"))
    assertFalse(GenerateTempMemoNameUseCase.isTemporaryName(""))
    assertFalse(GenerateTempMemoNameUseCase.isTemporaryName("LOCAL_123"))
  }

  @Test
  fun `when generating multiple names then all are unique`() {
    val names = (1..100).map { GenerateTempMemoNameUseCase() }.toSet()
    // All generated names should be unique
    assertTrue(names.size == 100)
  }

  @Test
  fun `when generated name then matches expected format`() {
    val name = GenerateTempMemoNameUseCase()
    // Format: local_{timestamp}_{random}
    val parts = name.removePrefix("local_").split("_")
    assertTrue(parts.size == 2)
    // First part is timestamp (long number)
    assertTrue(parts[0].all { it.isDigit() })
    // Second part is random (5 digit number)
    assertTrue(parts[1].length == 5)
    assertTrue(parts[1].all { it.isDigit() })
  }
}
