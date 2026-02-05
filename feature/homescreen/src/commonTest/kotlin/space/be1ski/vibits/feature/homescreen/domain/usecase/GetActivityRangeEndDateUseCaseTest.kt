package space.be1ski.vibits.feature.homescreen.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals

class GetActivityRangeEndDateUseCaseTest {
  @Test
  fun `when Week range then returns last day of week`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 1, 14), result)
  }

  @Test
  fun `when January range then returns last day of January`() {
    val range = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 1, 31), result)
  }

  @Test
  fun `when February in leap year then returns 29th`() {
    val range = ActivityRange.Month(year = 2024, month = Month.FEBRUARY)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 2, 29), result)
  }

  @Test
  fun `when February in non-leap year then returns 28th`() {
    val range = ActivityRange.Month(year = 2023, month = Month.FEBRUARY)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2023, 2, 28), result)
  }

  @Test
  fun `when April range then returns last day of April`() {
    val range = ActivityRange.Month(year = 2024, month = Month.APRIL)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 4, 30), result)
  }

  @Test
  fun `when Q1 range then returns last day of Q1`() {
    val range = ActivityRange.Quarter(year = 2024, index = 1)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 3, 31), result)
  }

  @Test
  fun `when Q2 range then returns last day of Q2`() {
    val range = ActivityRange.Quarter(year = 2024, index = 2)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 6, 30), result)
  }

  @Test
  fun `when Q3 range then returns last day of Q3`() {
    val range = ActivityRange.Quarter(year = 2024, index = 3)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 9, 30), result)
  }

  @Test
  fun `when Q4 range then returns last day of Q4`() {
    val range = ActivityRange.Quarter(year = 2024, index = 4)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 12, 31), result)
  }

  @Test
  fun `when Year range then returns last day of year`() {
    val range = ActivityRange.Year(year = 2024)

    val result = GetActivityRangeEndDateUseCase(range)

    assertEquals(LocalDate(2024, 12, 31), result)
  }
}
