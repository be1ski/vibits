package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigateActivityRangeUseCaseTest {
  @Test
  fun `when delta is 1 for week then shifts forward by 7 days`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    val result = NavigateActivityRangeUseCase(range, 1)

    assertEquals(LocalDate(2024, 1, 15), (result as ActivityRange.Week).startDate)
  }

  @Test
  fun `when delta is -1 for week then shifts backward by 7 days`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))

    val result = NavigateActivityRangeUseCase(range, -1)

    assertEquals(LocalDate(2024, 1, 8), (result as ActivityRange.Week).startDate)
  }

  @Test
  fun `when delta is 1 for month then shifts to next month`() {
    val range = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    val result = NavigateActivityRangeUseCase(range, 1)

    assertEquals(2024, (result as ActivityRange.Month).year)
    assertEquals(Month.FEBRUARY, result.month)
  }

  @Test
  fun `when delta is 1 for December then shifts to January of next year`() {
    val range = ActivityRange.Month(year = 2024, month = Month.DECEMBER)

    val result = NavigateActivityRangeUseCase(range, 1)

    assertEquals(2025, (result as ActivityRange.Month).year)
    assertEquals(Month.JANUARY, result.month)
  }

  @Test
  fun `when delta is 1 for quarter then shifts to next quarter`() {
    val range = ActivityRange.Quarter(year = 2024, index = 1)

    val result = NavigateActivityRangeUseCase(range, 1)

    assertEquals(2024, (result as ActivityRange.Quarter).year)
    assertEquals(2, result.index)
  }

  @Test
  fun `when delta is 1 for Q4 then shifts to Q1 of next year`() {
    val range = ActivityRange.Quarter(year = 2024, index = 4)

    val result = NavigateActivityRangeUseCase(range, 1)

    assertEquals(2025, (result as ActivityRange.Quarter).year)
    assertEquals(1, result.index)
  }

  @Test
  fun `when delta is -1 for Q1 then shifts to Q4 of previous year`() {
    val range = ActivityRange.Quarter(year = 2024, index = 1)

    val result = NavigateActivityRangeUseCase(range, -1)

    assertEquals(2023, (result as ActivityRange.Quarter).year)
    assertEquals(4, result.index)
  }

  @Test
  fun `when delta is 1 for year then shifts to next year`() {
    val range = ActivityRange.Year(year = 2024)

    val result = NavigateActivityRangeUseCase(range, 1)

    assertEquals(2025, (result as ActivityRange.Year).year)
  }
}
