package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateActivityRangesUseCaseTest {
  @Test
  fun `when single week span then generates one week`() {
    val startDate = LocalDate(2024, 1, 8) // Monday
    val endDate = LocalDate(2024, 1, 14) // Sunday

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val weeks = result.filterIsInstance<ActivityRange.Week>()

    assertEquals(1, weeks.size)
    assertEquals(LocalDate(2024, 1, 8), weeks.first().startDate)
  }

  @Test
  fun `when two week span then generates two weeks`() {
    val startDate = LocalDate(2024, 1, 8) // Monday
    val endDate = LocalDate(2024, 1, 21) // Sunday

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val weeks = result.filterIsInstance<ActivityRange.Week>()

    assertEquals(2, weeks.size)
    assertEquals(LocalDate(2024, 1, 8), weeks[0].startDate)
    assertEquals(LocalDate(2024, 1, 15), weeks[1].startDate)
  }

  @Test
  fun `when single month span then generates one month`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 1, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val months = result.filterIsInstance<ActivityRange.Month>()

    assertEquals(1, months.size)
    assertEquals(2024, months.first().year)
    assertEquals(Month.JANUARY, months.first().month)
  }

  @Test
  fun `when two month span then generates two months`() {
    val startDate = LocalDate(2024, 1, 15)
    val endDate = LocalDate(2024, 2, 15)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val months = result.filterIsInstance<ActivityRange.Month>()

    assertEquals(2, months.size)
    assertEquals(Month.JANUARY, months[0].month)
    assertEquals(Month.FEBRUARY, months[1].month)
  }

  @Test
  fun `when cross year months then generates correct months`() {
    val startDate = LocalDate(2023, 11, 1)
    val endDate = LocalDate(2024, 2, 28)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val months = result.filterIsInstance<ActivityRange.Month>()

    assertEquals(4, months.size)
    assertEquals(ActivityRange.Month(2023, Month.NOVEMBER), months[0])
    assertEquals(ActivityRange.Month(2023, Month.DECEMBER), months[1])
    assertEquals(ActivityRange.Month(2024, Month.JANUARY), months[2])
    assertEquals(ActivityRange.Month(2024, Month.FEBRUARY), months[3])
  }

  @Test
  fun `when single quarter span then generates one quarter`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 3, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val quarters = result.filterIsInstance<ActivityRange.Quarter>()

    assertEquals(1, quarters.size)
    assertEquals(2024, quarters.first().year)
    assertEquals(1, quarters.first().index)
  }

  @Test
  fun `when two quarter span then generates two quarters`() {
    val startDate = LocalDate(2024, 2, 1)
    val endDate = LocalDate(2024, 5, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val quarters = result.filterIsInstance<ActivityRange.Quarter>()

    assertEquals(2, quarters.size)
    assertEquals(ActivityRange.Quarter(2024, 1), quarters[0])
    assertEquals(ActivityRange.Quarter(2024, 2), quarters[1])
  }

  @Test
  fun `when cross year quarters then generates correct quarters`() {
    val startDate = LocalDate(2023, 10, 1) // Q4 2023
    val endDate = LocalDate(2024, 6, 30) // Q2 2024

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val quarters = result.filterIsInstance<ActivityRange.Quarter>()

    assertEquals(3, quarters.size)
    assertEquals(ActivityRange.Quarter(2023, 4), quarters[0])
    assertEquals(ActivityRange.Quarter(2024, 1), quarters[1])
    assertEquals(ActivityRange.Quarter(2024, 2), quarters[2])
  }

  @Test
  fun `when single year span then generates one year`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 12, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val years = result.filterIsInstance<ActivityRange.Year>()

    assertEquals(1, years.size)
    assertEquals(2024, years.first().year)
  }

  @Test
  fun `when two year span then generates two years`() {
    val startDate = LocalDate(2023, 6, 1)
    val endDate = LocalDate(2024, 6, 30)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val years = result.filterIsInstance<ActivityRange.Year>()

    assertEquals(2, years.size)
    assertEquals(2023, years[0].year)
    assertEquals(2024, years[1].year)
  }

  @Test
  fun `when multi year span then generates all years`() {
    val startDate = LocalDate(2020, 1, 1)
    val endDate = LocalDate(2024, 12, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val years = result.filterIsInstance<ActivityRange.Year>()

    assertEquals(5, years.size)
    assertEquals(listOf(2020, 2021, 2022, 2023, 2024), years.map { it.year })
  }

  @Test
  fun `when generates all range types then result contains all types`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 12, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)

    assertTrue(result.any { it is ActivityRange.Week })
    assertTrue(result.any { it is ActivityRange.Month })
    assertTrue(result.any { it is ActivityRange.Quarter })
    assertTrue(result.any { it is ActivityRange.Year })
  }

  @Test
  fun `when same start and end date then generates minimal ranges`() {
    val date = LocalDate(2024, 1, 15)

    val result = GenerateActivityRangesUseCase(date, date)

    val weeks = result.filterIsInstance<ActivityRange.Week>()
    val months = result.filterIsInstance<ActivityRange.Month>()
    val quarters = result.filterIsInstance<ActivityRange.Quarter>()
    val years = result.filterIsInstance<ActivityRange.Year>()

    assertEquals(1, weeks.size)
    assertEquals(1, months.size)
    assertEquals(1, quarters.size)
    assertEquals(1, years.size)
  }
}
