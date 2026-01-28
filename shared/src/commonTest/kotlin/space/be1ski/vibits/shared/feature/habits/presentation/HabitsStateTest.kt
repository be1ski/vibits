package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitsStateTest {
  @Test
  fun `when getActivityData with cached data then returns cached data`() {
    val range = ActivityRange.Week(LocalDate(2024, 1, 15))
    val mode = ActivityMode.HABITS
    val weekData = ActivityWeekData(emptyList(), 10, 5)
    val successRate = SuccessRateData(10, 10, 100f)
    val cached = CachedActivityData(weekData, emptyList(), successRate)

    val state =
      HabitsState(
        activityDataCache = mapOf(ActivityCacheKey(range, mode) to cached),
      )

    val result = state.getActivityData(range, mode)

    assertNotNull(result)
    assertEquals(weekData, result.weekData)
    assertEquals(successRate, result.successRate)
  }

  @Test
  fun `when getActivityData without cached data then returns null`() {
    val state = HabitsState(activityDataCache = emptyMap())

    val result = state.getActivityData(ActivityRange.Week(LocalDate(2024, 1, 15)), ActivityMode.HABITS)

    assertNull(result)
  }

  @Test
  fun `when isDataLoading with key in isRecalculating then returns true`() {
    val range = ActivityRange.Week(LocalDate(2024, 1, 15))
    val mode = ActivityMode.HABITS
    val state =
      HabitsState(
        isRecalculating = setOf(ActivityCacheKey(range, mode)),
      )

    val result = state.isDataLoading(range, mode)

    assertTrue(result)
  }

  @Test
  fun `when isDataLoading with key not in isRecalculating then returns false`() {
    val state = HabitsState(isRecalculating = emptySet())

    val result = state.isDataLoading(ActivityRange.Week(LocalDate(2024, 1, 15)), ActivityMode.HABITS)

    assertFalse(result)
  }

  @Test
  fun `when multiple cached entries then getActivityData returns correct one`() {
    val range1 = ActivityRange.Week(LocalDate(2024, 1, 15))
    val range2 = ActivityRange.Month(2024, Month.JANUARY)
    val weekData1 = ActivityWeekData(emptyList(), 10, 5)
    val weekData2 = ActivityWeekData(emptyList(), 20, 15)

    val state =
      HabitsState(
        activityDataCache =
          mapOf(
            ActivityCacheKey(range1, ActivityMode.HABITS) to CachedActivityData(weekData1, emptyList(), null),
            ActivityCacheKey(range2, ActivityMode.HABITS) to CachedActivityData(weekData2, emptyList(), null),
          ),
      )

    val result1 = state.getActivityData(range1, ActivityMode.HABITS)
    val result2 = state.getActivityData(range2, ActivityMode.HABITS)

    assertNotNull(result1)
    assertNotNull(result2)
    assertEquals(10, result1.weekData.maxDaily)
    assertEquals(20, result2.weekData.maxDaily)
  }
}
