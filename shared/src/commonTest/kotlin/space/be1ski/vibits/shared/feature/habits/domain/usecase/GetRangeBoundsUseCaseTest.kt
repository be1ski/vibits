package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRangeBoundsUseCaseTest {
  @Test
  fun `when range is Week then returns 7 day range`() {
    val startDate = LocalDate(2024, Month.JANUARY, 15)
    val range = ActivityRange.Week(startDate)

    val result = GetRangeBoundsUseCase(range)

    assertEquals(LocalDate(2024, Month.JANUARY, 15), result.start)
    assertEquals(LocalDate(2024, Month.JANUARY, 21), result.end)
  }

  @Test
  fun `when range is Month then returns full month`() {
    val range = ActivityRange.Month(2024, Month.FEBRUARY)

    val result = GetRangeBoundsUseCase(range)

    assertEquals(LocalDate(2024, Month.FEBRUARY, 1), result.start)
    assertEquals(LocalDate(2024, Month.FEBRUARY, 29), result.end) // 2024 is leap year
  }

  @Test
  fun `when range is Quarter 1 then returns Jan-Mar`() {
    val range = ActivityRange.Quarter(2024, 1)

    val result = GetRangeBoundsUseCase(range)

    assertEquals(LocalDate(2024, Month.JANUARY, 1), result.start)
    assertEquals(LocalDate(2024, Month.MARCH, 31), result.end)
  }

  @Test
  fun `when range is Quarter 2 then returns Apr-Jun`() {
    val range = ActivityRange.Quarter(2024, 2)

    val result = GetRangeBoundsUseCase(range)

    assertEquals(LocalDate(2024, Month.APRIL, 1), result.start)
    assertEquals(LocalDate(2024, Month.JUNE, 30), result.end)
  }

  @Test
  fun `when range is Quarter 3 then returns Jul-Sep`() {
    val range = ActivityRange.Quarter(2024, 3)

    val result = GetRangeBoundsUseCase(range)

    assertEquals(LocalDate(2024, Month.JULY, 1), result.start)
    assertEquals(LocalDate(2024, Month.SEPTEMBER, 30), result.end)
  }

  @Test
  fun `when range is Quarter 4 then returns Oct-Dec`() {
    val range = ActivityRange.Quarter(2024, 4)

    val result = GetRangeBoundsUseCase(range)

    assertEquals(LocalDate(2024, Month.OCTOBER, 1), result.start)
    assertEquals(LocalDate(2024, Month.DECEMBER, 31), result.end)
  }

  @Test
  fun `when range is Year then returns full year`() {
    val range = ActivityRange.Year(2024)

    val result = GetRangeBoundsUseCase(range)

    assertEquals(LocalDate(2024, Month.JANUARY, 1), result.start)
    assertEquals(LocalDate(2024, Month.DECEMBER, 31), result.end)
  }
}
