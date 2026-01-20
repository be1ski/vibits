package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateActivityRangeDeltaUseCaseTest {
  @Test
  fun `when to week is after from week then returns positive delta`() {
    val from = ActivityRange.Week(startDate = LocalDate(2024, 1, 1))
    val to = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))

    assertEquals(2, CalculateActivityRangeDeltaUseCase(from, to))
  }

  @Test
  fun `when to week is before from week then returns negative delta`() {
    val from = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val to = ActivityRange.Week(startDate = LocalDate(2024, 1, 1))

    assertEquals(-2, CalculateActivityRangeDeltaUseCase(from, to))
  }

  @Test
  fun `when months are in same year then returns month difference`() {
    val from = ActivityRange.Month(year = 2024, month = Month.JANUARY)
    val to = ActivityRange.Month(year = 2024, month = Month.APRIL)

    assertEquals(3, CalculateActivityRangeDeltaUseCase(from, to))
  }

  @Test
  fun `when months span years then returns correct delta`() {
    val from = ActivityRange.Month(year = 2023, month = Month.NOVEMBER)
    val to = ActivityRange.Month(year = 2024, month = Month.FEBRUARY)

    assertEquals(3, CalculateActivityRangeDeltaUseCase(from, to))
  }

  @Test
  fun `when quarters are in same year then returns quarter difference`() {
    val from = ActivityRange.Quarter(year = 2024, index = 1)
    val to = ActivityRange.Quarter(year = 2024, index = 4)

    assertEquals(3, CalculateActivityRangeDeltaUseCase(from, to))
  }

  @Test
  fun `when quarters span years then returns correct delta`() {
    val from = ActivityRange.Quarter(year = 2023, index = 3)
    val to = ActivityRange.Quarter(year = 2024, index = 2)

    assertEquals(3, CalculateActivityRangeDeltaUseCase(from, to))
  }

  @Test
  fun `when comparing years then returns year difference`() {
    val from = ActivityRange.Year(year = 2020)
    val to = ActivityRange.Year(year = 2024)

    assertEquals(4, CalculateActivityRangeDeltaUseCase(from, to))
  }

  @Test
  fun `when week compared with non-week then returns zero`() {
    val week = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))
    val month = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    assertEquals(0, CalculateActivityRangeDeltaUseCase(week, month))
  }

  @Test
  fun `when month compared with non-month then returns zero`() {
    val month = ActivityRange.Month(year = 2024, month = Month.JANUARY)
    val quarter = ActivityRange.Quarter(year = 2024, index = 1)

    assertEquals(0, CalculateActivityRangeDeltaUseCase(month, quarter))
  }

  @Test
  fun `when quarter compared with non-quarter then returns zero`() {
    val quarter = ActivityRange.Quarter(year = 2024, index = 1)
    val year = ActivityRange.Year(year = 2024)

    assertEquals(0, CalculateActivityRangeDeltaUseCase(quarter, year))
  }

  @Test
  fun `when year compared with non-year then returns zero`() {
    val year = ActivityRange.Year(year = 2024)
    val week = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    assertEquals(0, CalculateActivityRangeDeltaUseCase(year, week))
  }

  @Test
  fun `when ranges are same then returns zero`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    assertEquals(0, CalculateActivityRangeDeltaUseCase(range, range))
  }
}
