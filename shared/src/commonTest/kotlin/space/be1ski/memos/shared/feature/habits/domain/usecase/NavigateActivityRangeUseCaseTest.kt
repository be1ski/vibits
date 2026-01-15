package space.be1ski.memos.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.memos.shared.core.ui.ActivityRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigateActivityRangeUseCaseTest {

  private val useCase = NavigateActivityRangeUseCase()

  @Test
  fun `shifts week forward by one`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    val result = useCase(range, 1)

    assertEquals(LocalDate(2024, 1, 15), (result as ActivityRange.Week).startDate)
  }

  @Test
  fun `shifts week backward by one`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))

    val result = useCase(range, -1)

    assertEquals(LocalDate(2024, 1, 8), (result as ActivityRange.Week).startDate)
  }

  @Test
  fun `shifts month forward by one`() {
    val range = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    val result = useCase(range, 1)

    assertEquals(2024, (result as ActivityRange.Month).year)
    assertEquals(Month.FEBRUARY, result.month)
  }

  @Test
  fun `shifts month forward across year boundary`() {
    val range = ActivityRange.Month(year = 2024, month = Month.DECEMBER)

    val result = useCase(range, 1)

    assertEquals(2025, (result as ActivityRange.Month).year)
    assertEquals(Month.JANUARY, result.month)
  }

  @Test
  fun `shifts quarter forward by one`() {
    val range = ActivityRange.Quarter(year = 2024, index = 1)

    val result = useCase(range, 1)

    assertEquals(2024, (result as ActivityRange.Quarter).year)
    assertEquals(2, result.index)
  }

  @Test
  fun `shifts quarter forward across year boundary`() {
    val range = ActivityRange.Quarter(year = 2024, index = 4)

    val result = useCase(range, 1)

    assertEquals(2025, (result as ActivityRange.Quarter).year)
    assertEquals(1, result.index)
  }

  @Test
  fun `shifts year forward by one`() {
    val range = ActivityRange.Year(year = 2024)

    val result = useCase(range, 1)

    assertEquals(2025, (result as ActivityRange.Year).year)
  }

  @Test
  fun `formats week label correctly`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    val label = useCase.formatLabel(range)

    assertEquals("Jan 8 - Jan 14", label)
  }

  @Test
  fun `formats month label correctly`() {
    val range = ActivityRange.Month(year = 2024, month = Month.MARCH)

    val label = useCase.formatLabel(range)

    assertEquals("Mar 2024", label)
  }

  @Test
  fun `formats quarter label correctly`() {
    val range = ActivityRange.Quarter(year = 2024, index = 2)

    val label = useCase.formatLabel(range)

    assertEquals("Q2 2024", label)
  }

  @Test
  fun `formats year label correctly`() {
    val range = ActivityRange.Year(year = 2024)

    val label = useCase.formatLabel(range)

    assertEquals("2024", label)
  }

  @Test
  fun `isBefore returns true for earlier week`() {
    val earlier = ActivityRange.Week(startDate = LocalDate(2024, 1, 1))
    val later = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    assertTrue(useCase.isBefore(earlier, later))
    assertFalse(useCase.isBefore(later, earlier))
  }

  @Test
  fun `isBefore returns true for earlier month`() {
    val earlier = ActivityRange.Month(year = 2024, month = Month.JANUARY)
    val later = ActivityRange.Month(year = 2024, month = Month.MARCH)

    assertTrue(useCase.isBefore(earlier, later))
    assertFalse(useCase.isBefore(later, earlier))
  }

  @Test
  fun `isBefore returns true for earlier quarter`() {
    val earlier = ActivityRange.Quarter(year = 2024, index = 1)
    val later = ActivityRange.Quarter(year = 2024, index = 3)

    assertTrue(useCase.isBefore(earlier, later))
    assertFalse(useCase.isBefore(later, earlier))
  }

  @Test
  fun `isBefore returns true for earlier year`() {
    val earlier = ActivityRange.Year(year = 2023)
    val later = ActivityRange.Year(year = 2024)

    assertTrue(useCase.isBefore(earlier, later))
    assertFalse(useCase.isBefore(later, earlier))
  }

  @Test
  fun `isBefore returns false for same range`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    assertFalse(useCase.isBefore(range, range))
  }

  @Test
  fun `isBefore returns false for different range types`() {
    val week = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))
    val month = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    assertFalse(useCase.isBefore(week, month))
    assertFalse(useCase.isBefore(month, week))
  }
}
