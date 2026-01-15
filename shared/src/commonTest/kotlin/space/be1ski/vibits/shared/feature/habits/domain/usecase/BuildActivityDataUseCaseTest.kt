package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BuildActivityDataUseCaseTest {

  private val useCase = BuildActivityDataUseCase()

  @Test
  fun `lastSevenDays returns last 7 in-range days`() {
    val days = (1..10).map { day ->
      createDay(LocalDate(2024, 1, day), inRange = true)
    }
    val weekData = createWeekData(days)

    val result = useCase.lastSevenDays(weekData)

    assertEquals(7, result.size)
    assertEquals(LocalDate(2024, 1, 4), result.first().date)
    assertEquals(LocalDate(2024, 1, 10), result.last().date)
  }

  @Test
  fun `lastSevenDays filters out non-inRange days`() {
    val days = listOf(
      createDay(LocalDate(2024, 1, 1), inRange = false),
      createDay(LocalDate(2024, 1, 2), inRange = true),
      createDay(LocalDate(2024, 1, 3), inRange = true),
      createDay(LocalDate(2024, 1, 4), inRange = false),
      createDay(LocalDate(2024, 1, 5), inRange = true)
    )
    val weekData = createWeekData(days)

    val result = useCase.lastSevenDays(weekData)

    assertEquals(3, result.size)
    assertEquals(LocalDate(2024, 1, 2), result[0].date)
    assertEquals(LocalDate(2024, 1, 3), result[1].date)
    assertEquals(LocalDate(2024, 1, 5), result[2].date)
  }

  @Test
  fun `findDayByDate returns day for existing date`() {
    val days = listOf(
      createDay(LocalDate(2024, 1, 15)),
      createDay(LocalDate(2024, 1, 16))
    )
    val weekData = createWeekData(days)

    val result = useCase.findDayByDate(weekData, LocalDate(2024, 1, 15))

    assertNotNull(result)
    assertEquals(LocalDate(2024, 1, 15), result.date)
  }

  @Test
  fun `findDayByDate returns null for non-existing date`() {
    val days = listOf(
      createDay(LocalDate(2024, 1, 15))
    )
    val weekData = createWeekData(days)

    val result = useCase.findDayByDate(weekData, LocalDate(2024, 1, 20))

    assertNull(result)
  }

  @Test
  fun `activityWeekDataForHabit filters for single habit`() {
    val habit = HabitConfig(tag = "#habits/exercise", label = "Exercise")
    val days = listOf(
      createDayWithHabits(
        LocalDate(2024, 1, 15),
        listOf(
          HabitStatus(tag = "#habits/exercise", label = "Exercise", done = true),
          HabitStatus(tag = "#habits/reading", label = "Reading", done = false)
        )
      ),
      createDayWithHabits(
        LocalDate(2024, 1, 16),
        listOf(
          HabitStatus(tag = "#habits/exercise", label = "Exercise", done = false),
          HabitStatus(tag = "#habits/reading", label = "Reading", done = true)
        )
      )
    )
    val weekData = createWeekData(days)

    val result = useCase.activityWeekDataForHabit(weekData, habit)

    val day1 = result.weeks.first().days.first()
    assertEquals(1, day1.count)
    assertEquals(1, day1.totalHabits)
    assertEquals(1f, day1.completionRatio)

    val day2 = result.weeks.first().days.last()
    assertEquals(0, day2.count)
    assertEquals(1, day2.totalHabits)
    assertEquals(0f, day2.completionRatio)
  }

  @Test
  fun `activityWeekDataForHabit preserves days without habits`() {
    val habit = HabitConfig(tag = "#habits/exercise", label = "Exercise")
    val days = listOf(
      createDay(LocalDate(2024, 1, 15), totalHabits = 0)
    )
    val weekData = createWeekData(days)

    val result = useCase.activityWeekDataForHabit(weekData, habit)

    val day = result.weeks.first().days.first()
    assertEquals(0, day.count)
    assertEquals(0, day.totalHabits)
    assertEquals(0f, day.completionRatio)
  }

  private fun createDay(
    date: LocalDate,
    inRange: Boolean = true,
    totalHabits: Int = 1
  ): ContributionDay {
    return ContributionDay(
      date = date,
      count = 1,
      totalHabits = totalHabits,
      completionRatio = 1f,
      habitStatuses = emptyList(),
      dailyMemo = null,
      inRange = inRange
    )
  }

  private fun createDayWithHabits(
    date: LocalDate,
    habitStatuses: List<HabitStatus>
  ): ContributionDay {
    val completed = habitStatuses.count { it.done }
    val total = habitStatuses.size
    return ContributionDay(
      date = date,
      count = completed,
      totalHabits = total,
      completionRatio = if (total > 0) completed.toFloat() / total else 0f,
      habitStatuses = habitStatuses,
      dailyMemo = null,
      inRange = true
    )
  }

  private fun createWeekData(days: List<ContributionDay>): ActivityWeekData {
    val weeks = days.chunked(7).map { chunk ->
      ActivityWeek(
        startDate = chunk.first().date,
        days = chunk,
        weeklyCount = chunk.sumOf { it.count }
      )
    }
    return ActivityWeekData(
      weeks = weeks,
      maxDaily = days.maxOfOrNull { it.count } ?: 0,
      maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
    )
  }
}
