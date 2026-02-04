package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateActivityRangesUseCaseTest {
  @Test
  fun `when same start and end date then returns minimal ranges`() {
    val date = LocalDate(2024, 3, 15)

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

  @Test
  fun `when generates weeks then weeks start on Monday`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 1, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val weeks = result.filterIsInstance<ActivityRange.Week>()

    assertTrue(weeks.isNotEmpty())
    weeks.forEach { week ->
      assertEquals(
        kotlinx.datetime.DayOfWeek.MONDAY,
        week.startDate.dayOfWeek,
        "Week starting ${week.startDate} should start on Monday",
      )
    }
  }

  @Test
  fun `when generates weeks spanning January then returns correct weeks`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 1, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val weeks = result.filterIsInstance<ActivityRange.Week>()

    assertEquals(5, weeks.size)
    assertEquals(LocalDate(2024, 1, 1), weeks[0].startDate)
    assertEquals(LocalDate(2024, 1, 8), weeks[1].startDate)
    assertEquals(LocalDate(2024, 1, 15), weeks[2].startDate)
    assertEquals(LocalDate(2024, 1, 22), weeks[3].startDate)
    assertEquals(LocalDate(2024, 1, 29), weeks[4].startDate)
  }

  @Test
  fun `when generates months then returns correct months`() {
    val startDate = LocalDate(2024, 1, 15)
    val endDate = LocalDate(2024, 4, 10)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val months = result.filterIsInstance<ActivityRange.Month>()

    assertEquals(4, months.size)
    assertEquals(ActivityRange.Month(2024, Month.JANUARY), months[0])
    assertEquals(ActivityRange.Month(2024, Month.FEBRUARY), months[1])
    assertEquals(ActivityRange.Month(2024, Month.MARCH), months[2])
    assertEquals(ActivityRange.Month(2024, Month.APRIL), months[3])
  }

  @Test
  fun `when generates months spanning year boundary then works correctly`() {
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
  fun `when generates quarters then returns correct quarters`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 12, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val quarters = result.filterIsInstance<ActivityRange.Quarter>()

    assertEquals(4, quarters.size)
    assertEquals(ActivityRange.Quarter(2024, 1), quarters[0])
    assertEquals(ActivityRange.Quarter(2024, 2), quarters[1])
    assertEquals(ActivityRange.Quarter(2024, 3), quarters[2])
    assertEquals(ActivityRange.Quarter(2024, 4), quarters[3])
  }

  @Test
  fun `when generates quarters spanning year boundary then works correctly`() {
    val startDate = LocalDate(2023, 10, 1)
    val endDate = LocalDate(2024, 6, 30)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val quarters = result.filterIsInstance<ActivityRange.Quarter>()

    assertEquals(3, quarters.size)
    assertEquals(ActivityRange.Quarter(2023, 4), quarters[0])
    assertEquals(ActivityRange.Quarter(2024, 1), quarters[1])
    assertEquals(ActivityRange.Quarter(2024, 2), quarters[2])
  }

  @Test
  fun `when generates years then returns correct years`() {
    val startDate = LocalDate(2022, 6, 15)
    val endDate = LocalDate(2024, 3, 10)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val years = result.filterIsInstance<ActivityRange.Year>()

    assertEquals(3, years.size)
    assertEquals(ActivityRange.Year(2022), years[0])
    assertEquals(ActivityRange.Year(2023), years[1])
    assertEquals(ActivityRange.Year(2024), years[2])
  }

  @Test
  fun `when generates single year then returns one year`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 12, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)
    val years = result.filterIsInstance<ActivityRange.Year>()

    assertEquals(1, years.size)
    assertEquals(ActivityRange.Year(2024), years[0])
  }

  @Test
  fun `when result contains all range types then ordering is weeks, months, quarters, years`() {
    val startDate = LocalDate(2024, 1, 1)
    val endDate = LocalDate(2024, 1, 31)

    val result = GenerateActivityRangesUseCase(startDate, endDate)

    var seenMonth = false
    var seenQuarter = false
    var seenYear = false

    for (range in result) {
      when (range) {
        is ActivityRange.Week -> {
          assertTrue(!seenMonth && !seenQuarter && !seenYear, "Weeks should come first")
        }
        is ActivityRange.Month -> {
          assertTrue(!seenQuarter && !seenYear, "Months should come before quarters and years")
          seenMonth = true
        }
        is ActivityRange.Quarter -> {
          assertTrue(!seenYear, "Quarters should come before years")
          seenQuarter = true
        }
        is ActivityRange.Year -> {
          seenYear = true
        }
      }
    }
  }
}
