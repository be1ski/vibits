package space.be1ski.vibits.feature.homescreen.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals

class GetActivityRangeStartDateUseCaseTest {
  @Test
  fun `when Week range then returns week start date`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    val result = GetActivityRangeStartDateUseCase(range)

    assertEquals(LocalDate(2024, 1, 8), result)
  }

  @Test
  fun `when Month range then returns first day of month`() {
    val range = ActivityRange.Month(year = 2024, month = Month.MARCH)

    val result = GetActivityRangeStartDateUseCase(range)

    assertEquals(LocalDate(2024, 3, 1), result)
  }

  @Test
  fun `when Q1 range then returns first day of Q1`() {
    val range = ActivityRange.Quarter(year = 2024, index = 1)

    val result = GetActivityRangeStartDateUseCase(range)

    assertEquals(LocalDate(2024, 1, 1), result)
  }

  @Test
  fun `when Q2 range then returns first day of Q2`() {
    val range = ActivityRange.Quarter(year = 2024, index = 2)

    val result = GetActivityRangeStartDateUseCase(range)

    assertEquals(LocalDate(2024, 4, 1), result)
  }

  @Test
  fun `when Q3 range then returns first day of Q3`() {
    val range = ActivityRange.Quarter(year = 2024, index = 3)

    val result = GetActivityRangeStartDateUseCase(range)

    assertEquals(LocalDate(2024, 7, 1), result)
  }

  @Test
  fun `when Q4 range then returns first day of Q4`() {
    val range = ActivityRange.Quarter(year = 2024, index = 4)

    val result = GetActivityRangeStartDateUseCase(range)

    assertEquals(LocalDate(2024, 10, 1), result)
  }

  @Test
  fun `when Year range then returns first day of year`() {
    val range = ActivityRange.Year(year = 2024)

    val result = GetActivityRangeStartDateUseCase(range)

    assertEquals(LocalDate(2024, 1, 1), result)
  }
}
