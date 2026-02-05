package space.be1ski.vibits.feature.habits.domain.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ActivitySummaryTest {
  private fun createTestData(): ActivitySummary {
    val days =
      listOf(
        ContributionDay(
          date = LocalDate(2024, 1, 1),
          count = 3,
          totalHabits = 2,
          completionRatio = 1f,
          habitStatuses = emptyList(),
          dailyMemo = null,
          inRange = true,
        ),
        ContributionDay(
          date = LocalDate(2024, 1, 2),
          count = 5,
          totalHabits = 2,
          completionRatio = 1f,
          habitStatuses = emptyList(),
          dailyMemo = null,
          inRange = true,
        ),
        ContributionDay(
          date = LocalDate(2024, 1, 3),
          count = 2,
          totalHabits = 2,
          completionRatio = 0.5f,
          habitStatuses = emptyList(),
          dailyMemo = null,
          inRange = true,
        ),
        ContributionDay(
          date = LocalDate(2024, 1, 4),
          count = 0,
          totalHabits = 0,
          completionRatio = 0f,
          habitStatuses = emptyList(),
          dailyMemo = null,
          inRange = false,
        ),
      )
    val week = ActivityWeek(startDate = LocalDate(2024, 1, 1), days = days, weeklyCount = 10)
    return ActivitySummary(weeks = listOf(week), maxDaily = 5, maxWeekly = 10)
  }

  @Test
  fun `when lastSevenDays then returns only in-range days`() {
    val data = createTestData()

    val result = data.lastSevenDays()

    assertEquals(3, result.size)
    assertEquals(LocalDate(2024, 1, 1), result[0].date)
    assertEquals(LocalDate(2024, 1, 2), result[1].date)
    assertEquals(LocalDate(2024, 1, 3), result[2].date)
  }

  @Test
  fun `when lastSevenDays with more than 7 days then takes last 7`() {
    val days =
      (1..10).map { day ->
        ContributionDay(
          date = LocalDate(2024, 1, day),
          count = 1,
          totalHabits = 1,
          completionRatio = 1f,
          habitStatuses = emptyList(),
          dailyMemo = null,
          inRange = true,
        )
      }
    val week = ActivityWeek(startDate = LocalDate(2024, 1, 1), days = days, weeklyCount = 10)
    val data = ActivitySummary(weeks = listOf(week), maxDaily = 1, maxWeekly = 10)

    val result = data.lastSevenDays()

    assertEquals(7, result.size)
    assertEquals(LocalDate(2024, 1, 4), result[0].date)
    assertEquals(LocalDate(2024, 1, 10), result[6].date)
  }

  @Test
  fun `when findDayByDate with existing date then returns day`() {
    val data = createTestData()

    val result = data.findDayByDate(LocalDate(2024, 1, 2))

    assertNotNull(result)
    assertEquals(LocalDate(2024, 1, 2), result.date)
    assertEquals(5, result.count)
  }

  @Test
  fun `when findDayByDate with missing date then returns null`() {
    val data = createTestData()

    val result = data.findDayByDate(LocalDate(2024, 12, 31))

    assertNull(result)
  }

  @Test
  fun `when forHabit with matching habit then filters correctly`() {
    val habit = HabitConfig(tag = "exercise", label = "Exercise", color = 0L)
    val habitStatuses =
      listOf(
        HabitStatus(tag = "exercise", label = "Exercise", done = true),
        HabitStatus(tag = "reading", label = "Reading", done = false),
      )
    val days =
      listOf(
        ContributionDay(
          date = LocalDate(2024, 1, 1),
          count = 2,
          totalHabits = 2,
          completionRatio = 0.5f,
          habitStatuses = habitStatuses,
          dailyMemo = null,
          inRange = true,
        ),
      )
    val week = ActivityWeek(startDate = LocalDate(2024, 1, 1), days = days, weeklyCount = 2)
    val data = ActivitySummary(weeks = listOf(week), maxDaily = 2, maxWeekly = 2)

    val result = data.forHabit(habit)

    assertEquals(1, result.weeks.size)
    assertEquals(1, result.weeks[0].days.size)
    val day = result.weeks[0].days[0]
    assertEquals(1, day.count)
    assertEquals(1, day.totalHabits)
    assertEquals(1f, day.completionRatio)
    assertEquals(1, day.habitStatuses.size)
    assertEquals("exercise", day.habitStatuses[0].tag)
  }

  @Test
  fun `when forHabit with non-matching habit then returns zero counts`() {
    val habit = HabitConfig(tag = "meditation", label = "Meditation", color = 0L)
    val habitStatuses =
      listOf(
        HabitStatus(tag = "exercise", label = "Exercise", done = true),
      )
    val days =
      listOf(
        ContributionDay(
          date = LocalDate(2024, 1, 1),
          count = 1,
          totalHabits = 1,
          completionRatio = 1f,
          habitStatuses = habitStatuses,
          dailyMemo = null,
          inRange = true,
        ),
      )
    val week = ActivityWeek(startDate = LocalDate(2024, 1, 1), days = days, weeklyCount = 1)
    val data = ActivitySummary(weeks = listOf(week), maxDaily = 1, maxWeekly = 1)

    val result = data.forHabit(habit)

    assertEquals(1, result.weeks.size)
    val day = result.weeks[0].days[0]
    assertEquals(0, day.count)
    assertEquals(1, day.totalHabits)
    assertEquals(0f, day.completionRatio)
  }

  @Test
  fun `when forHabit with no config then returns zero totals`() {
    val habit = HabitConfig(tag = "exercise", label = "Exercise", color = 0L)
    val days =
      listOf(
        ContributionDay(
          date = LocalDate(2024, 1, 1),
          count = 0,
          totalHabits = 0,
          completionRatio = 0f,
          habitStatuses = emptyList(),
          dailyMemo = null,
          inRange = true,
        ),
      )
    val week = ActivityWeek(startDate = LocalDate(2024, 1, 1), days = days, weeklyCount = 0)
    val data = ActivitySummary(weeks = listOf(week), maxDaily = 0, maxWeekly = 0)

    val result = data.forHabit(habit)

    assertEquals(1, result.weeks.size)
    val day = result.weeks[0].days[0]
    assertEquals(0, day.count)
    assertEquals(0, day.totalHabits)
    assertEquals(0, day.habitStatuses.size)
  }

  @Test
  fun `when forHabit then recalculates maxDaily and maxWeekly`() {
    val habit = HabitConfig(tag = "exercise", label = "Exercise", color = 0L)
    val days1 =
      listOf(
        ContributionDay(
          date = LocalDate(2024, 1, 1),
          count = 2,
          totalHabits = 2,
          completionRatio = 0.5f,
          habitStatuses =
            listOf(
              HabitStatus(tag = "exercise", label = "Exercise", done = true),
              HabitStatus(tag = "reading", label = "Reading", done = false),
            ),
          dailyMemo = null,
          inRange = true,
        ),
        ContributionDay(
          date = LocalDate(2024, 1, 2),
          count = 2,
          totalHabits = 2,
          completionRatio = 1f,
          habitStatuses =
            listOf(
              HabitStatus(tag = "exercise", label = "Exercise", done = true),
              HabitStatus(tag = "reading", label = "Reading", done = true),
            ),
          dailyMemo = null,
          inRange = true,
        ),
      )
    val week = ActivityWeek(startDate = LocalDate(2024, 1, 1), days = days1, weeklyCount = 4)
    val data = ActivitySummary(weeks = listOf(week), maxDaily = 2, maxWeekly = 4)

    val result = data.forHabit(habit)

    assertEquals(1, result.maxDaily)
    assertEquals(2, result.maxWeekly)
  }
}
