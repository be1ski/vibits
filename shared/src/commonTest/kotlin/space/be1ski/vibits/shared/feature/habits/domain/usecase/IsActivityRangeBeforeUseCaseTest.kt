package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsActivityRangeBeforeUseCaseTest {
  @Test
  fun `when first week is earlier then returns true`() {
    val earlier = ActivityRange.Week(startDate = LocalDate(2024, 1, 1))
    val later = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    assertTrue(IsActivityRangeBeforeUseCase(earlier, later))
  }

  @Test
  fun `when first week is later then returns false`() {
    val earlier = ActivityRange.Week(startDate = LocalDate(2024, 1, 1))
    val later = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    assertFalse(IsActivityRangeBeforeUseCase(later, earlier))
  }

  @Test
  fun `when first month is earlier then returns true`() {
    val earlier = ActivityRange.Month(year = 2024, month = Month.JANUARY)
    val later = ActivityRange.Month(year = 2024, month = Month.MARCH)

    assertTrue(IsActivityRangeBeforeUseCase(earlier, later))
  }

  @Test
  fun `when first month is later then returns false`() {
    val earlier = ActivityRange.Month(year = 2024, month = Month.JANUARY)
    val later = ActivityRange.Month(year = 2024, month = Month.MARCH)

    assertFalse(IsActivityRangeBeforeUseCase(later, earlier))
  }

  @Test
  fun `when first month is in earlier year then returns true`() {
    val earlier = ActivityRange.Month(year = 2023, month = Month.DECEMBER)
    val later = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    assertTrue(IsActivityRangeBeforeUseCase(earlier, later))
  }

  @Test
  fun `when first month is in later year then returns false`() {
    val earlier = ActivityRange.Month(year = 2023, month = Month.DECEMBER)
    val later = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    assertFalse(IsActivityRangeBeforeUseCase(later, earlier))
  }

  @Test
  fun `when first quarter is earlier then returns true`() {
    val earlier = ActivityRange.Quarter(year = 2024, index = 1)
    val later = ActivityRange.Quarter(year = 2024, index = 3)

    assertTrue(IsActivityRangeBeforeUseCase(earlier, later))
  }

  @Test
  fun `when first quarter is later then returns false`() {
    val earlier = ActivityRange.Quarter(year = 2024, index = 1)
    val later = ActivityRange.Quarter(year = 2024, index = 3)

    assertFalse(IsActivityRangeBeforeUseCase(later, earlier))
  }

  @Test
  fun `when first quarter is in earlier year then returns true`() {
    val earlier = ActivityRange.Quarter(year = 2023, index = 4)
    val later = ActivityRange.Quarter(year = 2024, index = 1)

    assertTrue(IsActivityRangeBeforeUseCase(earlier, later))
  }

  @Test
  fun `when first quarter is in later year then returns false`() {
    val earlier = ActivityRange.Quarter(year = 2023, index = 4)
    val later = ActivityRange.Quarter(year = 2024, index = 1)

    assertFalse(IsActivityRangeBeforeUseCase(later, earlier))
  }

  @Test
  fun `when first year is earlier then returns true`() {
    val earlier = ActivityRange.Year(year = 2023)
    val later = ActivityRange.Year(year = 2024)

    assertTrue(IsActivityRangeBeforeUseCase(earlier, later))
  }

  @Test
  fun `when first year is later then returns false`() {
    val earlier = ActivityRange.Year(year = 2023)
    val later = ActivityRange.Year(year = 2024)

    assertFalse(IsActivityRangeBeforeUseCase(later, earlier))
  }

  @Test
  fun `when ranges are same then returns false`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))

    assertFalse(IsActivityRangeBeforeUseCase(range, range))
  }

  @Test
  fun `when range types differ then returns false`() {
    val week = ActivityRange.Week(startDate = LocalDate(2024, 1, 8))
    val month = ActivityRange.Month(year = 2024, month = Month.JANUARY)

    assertFalse(IsActivityRangeBeforeUseCase(week, month))
    assertFalse(IsActivityRangeBeforeUseCase(month, week))
  }
}
