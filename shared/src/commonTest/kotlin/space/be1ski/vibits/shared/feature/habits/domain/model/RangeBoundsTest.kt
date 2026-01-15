package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.core.ui.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals

class RangeBoundsTest {
  @Test
  fun `rangeBounds for Week returns 7 day range`() {
    val startDate = LocalDate(2024, Month.JANUARY, 15)
    val range = ActivityRange.Week(startDate)

    val result = rangeBounds(range)

    assertEquals(LocalDate(2024, Month.JANUARY, 15), result.start)
    assertEquals(LocalDate(2024, Month.JANUARY, 21), result.end)
  }

  @Test
  fun `rangeBounds for Month returns full month`() {
    val range = ActivityRange.Month(2024, Month.FEBRUARY)

    val result = rangeBounds(range)

    assertEquals(LocalDate(2024, Month.FEBRUARY, 1), result.start)
    assertEquals(LocalDate(2024, Month.FEBRUARY, 29), result.end) // 2024 is leap year
  }

  @Test
  fun `rangeBounds for Quarter 1 returns Jan-Mar`() {
    val range = ActivityRange.Quarter(2024, 1)

    val result = rangeBounds(range)

    assertEquals(LocalDate(2024, Month.JANUARY, 1), result.start)
    assertEquals(LocalDate(2024, Month.MARCH, 31), result.end)
  }

  @Test
  fun `rangeBounds for Quarter 2 returns Apr-Jun`() {
    val range = ActivityRange.Quarter(2024, 2)

    val result = rangeBounds(range)

    assertEquals(LocalDate(2024, Month.APRIL, 1), result.start)
    assertEquals(LocalDate(2024, Month.JUNE, 30), result.end)
  }

  @Test
  fun `rangeBounds for Quarter 3 returns Jul-Sep`() {
    val range = ActivityRange.Quarter(2024, 3)

    val result = rangeBounds(range)

    assertEquals(LocalDate(2024, Month.JULY, 1), result.start)
    assertEquals(LocalDate(2024, Month.SEPTEMBER, 30), result.end)
  }

  @Test
  fun `rangeBounds for Quarter 4 returns Oct-Dec`() {
    val range = ActivityRange.Quarter(2024, 4)

    val result = rangeBounds(range)

    assertEquals(LocalDate(2024, Month.OCTOBER, 1), result.start)
    assertEquals(LocalDate(2024, Month.DECEMBER, 31), result.end)
  }

  @Test
  fun `rangeBounds for Year returns full year`() {
    val range = ActivityRange.Year(2024)

    val result = rangeBounds(range)

    assertEquals(LocalDate(2024, Month.JANUARY, 1), result.start)
    assertEquals(LocalDate(2024, Month.DECEMBER, 31), result.end)
  }
}
