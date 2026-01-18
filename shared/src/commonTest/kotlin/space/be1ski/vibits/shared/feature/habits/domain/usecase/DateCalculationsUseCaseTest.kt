package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant as KtInstant

class DateCalculationsUseCaseTest {
  private val timeZone = TimeZone.UTC

  @Test
  fun `startOfWeek returns Monday for Monday`() {
    val monday = LocalDate(2024, 1, 8)

    val result = startOfWeek(monday)

    assertEquals(monday, result)
    assertEquals(DayOfWeek.MONDAY, result.dayOfWeek)
  }

  @Test
  fun `startOfWeek returns Monday for Wednesday`() {
    val wednesday = LocalDate(2024, 1, 10)
    val expectedMonday = LocalDate(2024, 1, 8)

    val result = startOfWeek(wednesday)

    assertEquals(expectedMonday, result)
    assertEquals(DayOfWeek.MONDAY, result.dayOfWeek)
  }

  @Test
  fun `startOfWeek returns Monday for Sunday`() {
    val sunday = LocalDate(2024, 1, 14)
    val expectedMonday = LocalDate(2024, 1, 8)

    val result = startOfWeek(sunday)

    assertEquals(expectedMonday, result)
    assertEquals(DayOfWeek.MONDAY, result.dayOfWeek)
  }

  @Test
  fun `startOfWeek handles year boundary`() {
    val thursday = LocalDate(2024, 1, 4)
    val expectedMonday = LocalDate(2024, 1, 1)

    val result = startOfWeek(thursday)

    assertEquals(expectedMonday, result)
  }

  @Test
  fun `quarterIndex returns 1 for January`() {
    assertEquals(1, quarterIndex(Month.JANUARY))
    assertEquals(1, quarterIndex(Month.FEBRUARY))
    assertEquals(1, quarterIndex(Month.MARCH))
  }

  @Test
  fun `quarterIndex returns 2 for Q2 months`() {
    assertEquals(2, quarterIndex(Month.APRIL))
    assertEquals(2, quarterIndex(Month.MAY))
    assertEquals(2, quarterIndex(Month.JUNE))
  }

  @Test
  fun `quarterIndex returns 3 for Q3 months`() {
    assertEquals(3, quarterIndex(Month.JULY))
    assertEquals(3, quarterIndex(Month.AUGUST))
    assertEquals(3, quarterIndex(Month.SEPTEMBER))
  }

  @Test
  fun `quarterIndex returns 4 for Q4 months`() {
    assertEquals(4, quarterIndex(Month.OCTOBER))
    assertEquals(4, quarterIndex(Month.NOVEMBER))
    assertEquals(4, quarterIndex(Month.DECEMBER))
  }

  @Test
  fun `quarterIndex for date delegates to month`() {
    val date = LocalDate(2024, 5, 15)

    assertEquals(2, quarterIndex(date))
  }

  @Test
  fun `earliestMemoDate returns earliest date from content`() {
    val memos =
      listOf(
        createMemo("#daily 2024-03-15", KtInstant.parse("2024-01-10T10:00:00Z")),
        createMemo("#daily 2024-01-01", KtInstant.parse("2024-01-15T10:00:00Z")),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertEquals(LocalDate(2024, 1, 1), result)
  }

  @Test
  fun `earliestMemoDate uses memo date when no date in content`() {
    val memos =
      listOf(
        createMemo("Regular content", KtInstant.parse("2024-01-10T10:00:00Z")),
        createMemo("Another content", KtInstant.parse("2024-01-15T10:00:00Z")),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertEquals(LocalDate(2024, 1, 10), result)
  }

  @Test
  fun `earliestMemoDate returns null for empty list`() {
    val result = EarliestMemoDateUseCase(emptyList(), timeZone)

    assertNull(result)
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
}
