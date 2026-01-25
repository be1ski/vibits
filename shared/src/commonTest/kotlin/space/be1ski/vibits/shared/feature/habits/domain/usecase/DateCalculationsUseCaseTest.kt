package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant as KtInstant

class DateCalculationsUseCaseTest {
  private val timeZone = TimeZone.UTC

  @Test
  fun `when date is Monday then startOfWeek returns same date`() {
    val monday = LocalDate(2024, 1, 8)

    val result = startOfWeek(monday)

    assertEquals(monday, result)
    assertEquals(DayOfWeek.MONDAY, result.dayOfWeek)
  }

  @Test
  fun `when date is Wednesday then startOfWeek returns Monday`() {
    val wednesday = LocalDate(2024, 1, 10)
    val expectedMonday = LocalDate(2024, 1, 8)

    val result = startOfWeek(wednesday)

    assertEquals(expectedMonday, result)
    assertEquals(DayOfWeek.MONDAY, result.dayOfWeek)
  }

  @Test
  fun `when date is Sunday then startOfWeek returns Monday`() {
    val sunday = LocalDate(2024, 1, 14)
    val expectedMonday = LocalDate(2024, 1, 8)

    val result = startOfWeek(sunday)

    assertEquals(expectedMonday, result)
    assertEquals(DayOfWeek.MONDAY, result.dayOfWeek)
  }

  @Test
  fun `when date crosses year boundary then startOfWeek returns correct Monday`() {
    val thursday = LocalDate(2024, 1, 4)
    val expectedMonday = LocalDate(2024, 1, 1)

    val result = startOfWeek(thursday)

    assertEquals(expectedMonday, result)
  }

  @Test
  fun `when month is Q1 then quarterIndex returns 1`() {
    assertEquals(1, quarterIndex(Month.JANUARY))
    assertEquals(1, quarterIndex(Month.FEBRUARY))
    assertEquals(1, quarterIndex(Month.MARCH))
  }

  @Test
  fun `when month is Q2 then quarterIndex returns 2`() {
    assertEquals(2, quarterIndex(Month.APRIL))
    assertEquals(2, quarterIndex(Month.MAY))
    assertEquals(2, quarterIndex(Month.JUNE))
  }

  @Test
  fun `when month is Q3 then quarterIndex returns 3`() {
    assertEquals(3, quarterIndex(Month.JULY))
    assertEquals(3, quarterIndex(Month.AUGUST))
    assertEquals(3, quarterIndex(Month.SEPTEMBER))
  }

  @Test
  fun `when month is Q4 then quarterIndex returns 4`() {
    assertEquals(4, quarterIndex(Month.OCTOBER))
    assertEquals(4, quarterIndex(Month.NOVEMBER))
    assertEquals(4, quarterIndex(Month.DECEMBER))
  }

  @Test
  fun `when date is in May then quarterIndex returns 2`() {
    val date = LocalDate(2024, 5, 15)

    assertEquals(2, quarterIndex(date))
  }

  @Test
  fun `when memos have dates in content then returns earliest content date`() {
    val memos =
      listOf(
        createMemo("${PostTags.DAILY} 2024-03-15", KtInstant.parse("2024-01-10T10:00:00Z")),
        createMemo("${PostTags.DAILY} 2024-01-01", KtInstant.parse("2024-01-15T10:00:00Z")),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertEquals(LocalDate(2024, 1, 1), result)
  }

  @Test
  fun `when memos have no date in content then returns earliest memo createTime`() {
    val memos =
      listOf(
        createMemo("Regular content", KtInstant.parse("2024-01-10T10:00:00Z")),
        createMemo("Another content", KtInstant.parse("2024-01-15T10:00:00Z")),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertEquals(LocalDate(2024, 1, 10), result)
  }

  @Test
  fun `when memos list is empty then returns null`() {
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
