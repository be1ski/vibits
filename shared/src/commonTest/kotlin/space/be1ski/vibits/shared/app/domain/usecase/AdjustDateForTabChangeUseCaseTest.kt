package space.be1ski.vibits.shared.app.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import kotlin.test.Test
import kotlin.test.assertEquals

class AdjustDateForTabChangeUseCaseTest {
  @Test
  fun `returns same date when switching to larger granularity`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.WEEKS, TimeRangeTab.MONTHS)

    assertEquals(date, result)
  }

  @Test
  fun `returns same date when switching to same granularity`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.MONTHS, TimeRangeTab.MONTHS)

    assertEquals(date, result)
  }

  @Test
  fun `returns same date when old tab is WEEKS`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.WEEKS, TimeRangeTab.WEEKS)

    assertEquals(date, result)
  }

  @Test
  fun `returns end of month when switching from MONTHS to WEEKS`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.MONTHS, TimeRangeTab.WEEKS)

    assertEquals(LocalDate(2024, 3, 31), result)
  }

  @Test
  fun `returns end of quarter when switching from QUARTERS to WEEKS`() {
    val date = LocalDate(2024, 2, 15) // Q1

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.QUARTERS, TimeRangeTab.WEEKS)

    assertEquals(LocalDate(2024, 3, 31), result)
  }

  @Test
  fun `returns end of quarter when switching from QUARTERS to MONTHS`() {
    val date = LocalDate(2024, 5, 15) // Q2

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.QUARTERS, TimeRangeTab.MONTHS)

    assertEquals(LocalDate(2024, 6, 30), result)
  }

  @Test
  fun `returns end of year when switching from YEARS to WEEKS`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.YEARS, TimeRangeTab.WEEKS)

    assertEquals(LocalDate(2024, 12, 31), result)
  }

  @Test
  fun `returns end of year when switching from YEARS to MONTHS`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.YEARS, TimeRangeTab.MONTHS)

    assertEquals(LocalDate(2024, 12, 31), result)
  }

  @Test
  fun `returns end of year when switching from YEARS to QUARTERS`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.YEARS, TimeRangeTab.QUARTERS)

    assertEquals(LocalDate(2024, 12, 31), result)
  }
}
