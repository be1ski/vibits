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
  private val timeZone = TimeZone.UTC

  @Test
  fun `when memo has config tag then extracts habits`() {
    val memo =
      createMemo(
        content = "#habits/config\n- [ ] exercise\n- [ ] reading",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractHabitsConfigUseCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertEquals(2, result.first().habits.size)
  }

  @Test
  fun `when memo has habits_config tag then extracts habits`() {
    val memo =
      createMemo(
        content = "#habits_config\n- [ ] meditation",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractHabitsConfigUseCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertEquals(1, result.first().habits.size)
  }

  @Test
  fun `when memo has no config tag then ignores it`() {
    val memo =
      createMemo(
        content = "Regular memo",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractHabitsConfigUseCase(listOf(memo), timeZone)

    assertEquals(0, result.size)
  }

  @Test
  fun `when multiple configs exist then sorts by date`() {
    val memo1 =
      createMemo(
        content = "#habits/config\n- [ ] task1",
        createTime = KtInstant.parse("2024-01-20T10:00:00Z"),
      )
    val memo2 =
      createMemo(
        content = "#habits/config\n- [ ] task2",
        createTime = KtInstant.parse("2024-01-10T10:00:00Z"),
      )

    val result = ExtractHabitsConfigUseCase(listOf(memo1, memo2), timeZone)

    assertEquals(2, result.size)
    assertEquals(LocalDate(2024, 1, 10), result.first().date)
    assertEquals(LocalDate(2024, 1, 20), result.last().date)
  }

  @Test
  fun `when forDate called then returns most recent config before date`() {
    val entries =
      listOf(
        createConfigEntry(LocalDate(2024, 1, 1)),
        createConfigEntry(LocalDate(2024, 1, 10)),
        createConfigEntry(LocalDate(2024, 1, 20)),
      )

    val result = ExtractHabitsConfigUseCase.forDate(entries, LocalDate(2024, 1, 15))

    assertNotNull(result)
    assertEquals(LocalDate(2024, 1, 10), result.date)
  }

  @Test
  fun `when forDate called with exact config date then returns that config`() {
    val entries =
      listOf(
        createConfigEntry(LocalDate(2024, 1, 1)),
        createConfigEntry(LocalDate(2024, 1, 15)),
      )

    val result = ExtractHabitsConfigUseCase.forDate(entries, LocalDate(2024, 1, 15))

    assertNotNull(result)
    assertEquals(LocalDate(2024, 1, 15), result.date)
  }

  @Test
  fun `when forDate called with date before all configs then returns null`() {
    val entries =
      listOf(
        createConfigEntry(LocalDate(2024, 2, 1)),
      )

    val result = ExtractHabitsConfigUseCase.forDate(entries, LocalDate(2024, 1, 15))

    assertNull(result)
  }

  @Test
  fun `when config has duplicate habits then deduplicates by tag`() {
    val memo =
      createMemo(
        content = "#habits/config\n- [ ] exercise\n- [ ] exercise\n- [ ] reading",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractHabitsConfigUseCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertEquals(2, result.first().habits.size)
  }

  private fun createMemo(
    content: String,
    createTime: KtInstant,
  ): Memo =
    Memo(
      name = "memos/test",
      content = content,
      createTime = createTime,
      updateTime = null,
    )

  private fun createConfigEntry(date: LocalDate): HabitsConfigEntry =
    HabitsConfigEntry(
      date = date,
      habits = emptyList(),
      memo = createMemo("#habits/config", KtInstant.parse("2024-01-01T00:00:00Z")),
    )
}
