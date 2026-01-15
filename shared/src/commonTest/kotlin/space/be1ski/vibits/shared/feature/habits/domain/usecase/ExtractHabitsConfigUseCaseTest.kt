package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant as KtInstant

class ExtractHabitsConfigUseCaseTest {

  private val useCase = ExtractHabitsConfigUseCase()
  private val timeZone = TimeZone.UTC

  @Test
  fun `extracts habits config with config tag`() {
    val memo = createMemo(
      content = "#habits/config\n- [ ] exercise\n- [ ] reading",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertEquals(2, result.first().habits.size)
  }

  @Test
  fun `extracts habits config with habits_config tag`() {
    val memo = createMemo(
      content = "#habits_config\n- [ ] meditation",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertEquals(1, result.first().habits.size)
  }

  @Test
  fun `ignores non-config memos`() {
    val memo = createMemo(
      content = "Regular memo",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertEquals(0, result.size)
  }

  @Test
  fun `sorts config entries by date`() {
    val memo1 = createMemo(
      content = "#habits/config\n- [ ] task1",
      createTime = KtInstant.parse("2024-01-20T10:00:00Z")
    )
    val memo2 = createMemo(
      content = "#habits/config\n- [ ] task2",
      createTime = KtInstant.parse("2024-01-10T10:00:00Z")
    )

    val result = useCase(listOf(memo1, memo2), timeZone)

    assertEquals(2, result.size)
    assertEquals(LocalDate(2024, 1, 10), result.first().date)
    assertEquals(LocalDate(2024, 1, 20), result.last().date)
  }

  @Test
  fun `forDate returns most recent config before date`() {
    val entries = listOf(
      createConfigEntry(LocalDate(2024, 1, 1)),
      createConfigEntry(LocalDate(2024, 1, 10)),
      createConfigEntry(LocalDate(2024, 1, 20))
    )

    val result = useCase.forDate(entries, LocalDate(2024, 1, 15))

    assertNotNull(result)
    assertEquals(LocalDate(2024, 1, 10), result.date)
  }

  @Test
  fun `forDate returns config on exact date`() {
    val entries = listOf(
      createConfigEntry(LocalDate(2024, 1, 1)),
      createConfigEntry(LocalDate(2024, 1, 15))
    )

    val result = useCase.forDate(entries, LocalDate(2024, 1, 15))

    assertNotNull(result)
    assertEquals(LocalDate(2024, 1, 15), result.date)
  }

  @Test
  fun `forDate returns null when no config before date`() {
    val entries = listOf(
      createConfigEntry(LocalDate(2024, 2, 1))
    )

    val result = useCase.forDate(entries, LocalDate(2024, 1, 15))

    assertNull(result)
  }

  @Test
  fun `deduplicates habits by tag`() {
    val memo = createMemo(
      content = "#habits/config\n- [ ] exercise\n- [ ] exercise\n- [ ] reading",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertEquals(2, result.first().habits.size)
  }

  private fun createMemo(
    content: String,
    createTime: KtInstant
  ): Memo {
    return Memo(
      name = "memos/test",
      content = content,
      createTime = createTime,
      updateTime = null
    )
  }

  private fun createConfigEntry(date: LocalDate): HabitsConfigEntry {
    return HabitsConfigEntry(
      date = date,
      habits = emptyList(),
      memo = createMemo("#habits/config", KtInstant.parse("2024-01-01T00:00:00Z"))
    )
  }
}
