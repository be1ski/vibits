package space.be1ski.vibits.shared.core.date

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatterTest {
  private val formatter =
    DateFormatter(
      months =
        mapOf(
          Month.JANUARY to "Jan",
          Month.FEBRUARY to "Feb",
          Month.MARCH to "Mar",
          Month.DECEMBER to "Dec",
        ),
      days =
        mapOf(
          DayOfWeek.MONDAY to "Mo",
          DayOfWeek.TUESDAY to "Tu",
          DayOfWeek.WEDNESDAY to "We",
        ),
    )

  @Test
  fun `when monthShort with known month then returns mapped value`() {
    assertEquals("Jan", formatter.monthShort(Month.JANUARY))
    assertEquals("Feb", formatter.monthShort(Month.FEBRUARY))
  }

  @Test
  fun `when monthShort with unknown month then returns fallback`() {
    assertEquals("APR", formatter.monthShort(Month.APRIL))
  }

  @Test
  fun `when dayOfWeekShort with known day then returns mapped value`() {
    assertEquals("Mo", formatter.dayOfWeekShort(DayOfWeek.MONDAY))
    assertEquals("Tu", formatter.dayOfWeekShort(DayOfWeek.TUESDAY))
  }

  @Test
  fun `when dayOfWeekShort with unknown day then returns fallback`() {
    assertEquals("TH", formatter.dayOfWeekShort(DayOfWeek.THURSDAY))
  }

  @Test
  fun `when monthInitial then returns first character`() {
    assertEquals("J", formatter.monthInitial(Month.JANUARY))
    assertEquals("A", formatter.monthInitial(Month.APRIL))
  }

  @Test
  fun `when monthDay then returns month and day`() {
    val date = LocalDate(2024, 1, 15)
    assertEquals("Jan 15", formatter.monthDay(date))
  }

  @Test
  fun `when dateTime then returns formatted datetime`() {
    val dt = LocalDateTime(2024, 1, 15, 9, 5)
    assertEquals("2024-01-15 09:05", formatter.dateTime(dt))
  }

  @Test
  fun `when compactDateTime then returns compact format`() {
    val dt = LocalDateTime(2024, 3, 7, 14, 30)
    assertEquals("7/3 14:30", formatter.compactDateTime(dt))
  }

  @Test
  fun `when weekRange same year as current then omits year`() {
    val start = LocalDate(2024, 1, 1)
    val end = LocalDate(2024, 1, 7)

    assertEquals("Jan 1 - Jan 7", formatter.weekRange(start, end, 2024))
  }

  @Test
  fun `when weekRange different year than current then shows year`() {
    val start = LocalDate(2023, 12, 25)
    val end = LocalDate(2023, 12, 31)

    assertEquals("Dec 25 - Dec 31 (2023)", formatter.weekRange(start, end, 2024))
  }

  @Test
  fun `when weekRange spans years then shows both years`() {
    val start = LocalDate(2023, 12, 25)
    val end = LocalDate(2024, 1, 7)

    assertEquals("Dec 25, 2023 – Jan 7, 2024", formatter.weekRange(start, end, 2024))
  }
}
