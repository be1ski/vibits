package space.be1ski.vibits.feature.main.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import kotlin.test.Test
import kotlin.test.assertEquals

class AdjustDateForTabChangeUseCaseTest {
  @Test
  fun `when switching to larger granularity then returns same date`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.WEEKS, TimeRangeTab.MONTHS)

    assertEquals(date, result)
  }

  @Test
  fun `when switching to same granularity then returns same date`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.MONTHS, TimeRangeTab.MONTHS)

    assertEquals(date, result)
  }

  @Test
  fun `when old tab is WEEKS then returns same date`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.WEEKS, TimeRangeTab.WEEKS)

    assertEquals(date, result)
  }

  @Test
  fun `when switching from MONTHS to WEEKS then returns end of month`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.MONTHS, TimeRangeTab.WEEKS)

    assertEquals(LocalDate(2024, 3, 31), result)
  }

  @Test
  fun `when switching from QUARTERS to WEEKS then returns end of quarter`() {
    val date = LocalDate(2024, 2, 15) // Q1

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.QUARTERS, TimeRangeTab.WEEKS)

    assertEquals(LocalDate(2024, 3, 31), result)
  }

  @Test
  fun `when switching from QUARTERS to MONTHS then returns end of quarter`() {
    val date = LocalDate(2024, 5, 15) // Q2

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.QUARTERS, TimeRangeTab.MONTHS)

    assertEquals(LocalDate(2024, 6, 30), result)
  }

  @Test
  fun `when switching from YEARS to WEEKS then returns end of year`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.YEARS, TimeRangeTab.WEEKS)

    assertEquals(LocalDate(2024, 12, 31), result)
  }

  @Test
  fun `when switching from YEARS to MONTHS then returns end of year`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.YEARS, TimeRangeTab.MONTHS)

    assertEquals(LocalDate(2024, 12, 31), result)
  }

  @Test
  fun `when switching from YEARS to QUARTERS then returns end of year`() {
    val date = LocalDate(2024, 3, 15)

    val result = AdjustDateForTabChangeUseCase(date, TimeRangeTab.YEARS, TimeRangeTab.QUARTERS)

    assertEquals(LocalDate(2024, 12, 31), result)
  }
}
