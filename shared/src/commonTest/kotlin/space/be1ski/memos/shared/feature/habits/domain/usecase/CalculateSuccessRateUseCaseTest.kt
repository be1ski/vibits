package space.be1ski.memos.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.core.ui.ActivityRange
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateSuccessRateUseCaseTest {

  private val useCase = CalculateSuccessRateUseCase()

  @Test
  fun `when current week and today is Thursday then calculates Monday to Thursday`() {
    val monday = LocalDate(2024, 1, 8)
    val thursday = LocalDate(2024, 1, 11)
    val weekData = createWeekData(
      monday to dayWithHabits(monday, completed = 2, total = 3),
      LocalDate(2024, 1, 9) to dayWithHabits(LocalDate(2024, 1, 9), completed = 3, total = 3),
      LocalDate(2024, 1, 10) to dayWithHabits(LocalDate(2024, 1, 10), completed = 1, total = 3),
      thursday to dayWithHabits(thursday, completed = 2, total = 3),
      LocalDate(2024, 1, 12) to dayWithHabits(LocalDate(2024, 1, 12), completed = 3, total = 3),
      LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), completed = 3, total = 3),
      LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), completed = 3, total = 3)
    )
    val range = ActivityRange.Week(startDate = monday)

    val result = useCase(weekData, range, today = thursday)

    assertEquals(8, result.completed)
    assertEquals(12, result.total)
    assertEquals(0.667f, result.rate, 0.01f)
  }

  @Test
  fun `when past week then calculates full week`() {
    val monday = LocalDate(2024, 1, 1)
    val weekData = createWeekData(
      monday to dayWithHabits(monday, completed = 2, total = 2),
      LocalDate(2024, 1, 2) to dayWithHabits(LocalDate(2024, 1, 2), completed = 2, total = 2),
      LocalDate(2024, 1, 3) to dayWithHabits(LocalDate(2024, 1, 3), completed = 1, total = 2),
      LocalDate(2024, 1, 4) to dayWithHabits(LocalDate(2024, 1, 4), completed = 2, total = 2),
      LocalDate(2024, 1, 5) to dayWithHabits(LocalDate(2024, 1, 5), completed = 2, total = 2),
      LocalDate(2024, 1, 6) to dayWithHabits(LocalDate(2024, 1, 6), completed = 2, total = 2),
      LocalDate(2024, 1, 7) to dayWithHabits(LocalDate(2024, 1, 7), completed = 2, total = 2)
    )
    val range = ActivityRange.Week(startDate = monday)
    val today = LocalDate(2024, 1, 15)

    val result = useCase(weekData, range, today)

    assertEquals(13, result.completed)
    assertEquals(14, result.total)
    assertEquals(0.929f, result.rate, 0.01f)
  }

  @Test
  fun `when current month and today is 17th then calculates 1st to 17th`() {
    val days = (1..31).map { day ->
      val date = LocalDate(2024, 1, day)
      date to dayWithHabits(date, completed = if (day <= 17) 1 else 2, total = 2)
    }
    val weekData = createWeekData(*days.toTypedArray())
    val range = ActivityRange.Month(year = 2024, month = kotlinx.datetime.Month.JANUARY)
    val today = LocalDate(2024, 1, 17)

    val result = useCase(weekData, range, today)

    assertEquals(17, result.completed)
    assertEquals(34, result.total)
    assertEquals(0.5f, result.rate, 0.01f)
  }

  @Test
  fun `when past month then calculates full month`() {
    val days = (1..31).map { day ->
      val date = LocalDate(2024, 1, day)
      date to dayWithHabits(date, completed = 2, total = 2)
    }
    val weekData = createWeekData(*days.toTypedArray())
    val range = ActivityRange.Month(year = 2024, month = kotlinx.datetime.Month.JANUARY)
    val today = LocalDate(2024, 2, 15)

    val result = useCase(weekData, range, today)

    assertEquals(62, result.completed)
    assertEquals(62, result.total)
    assertEquals(1f, result.rate, 0.01f)
  }

  @Test
  fun `when zero total then rate is zero`() {
    val monday = LocalDate(2024, 1, 8)
    val weekData = createWeekData(
      monday to dayWithHabits(monday, completed = 0, total = 0)
    )
    val range = ActivityRange.Week(startDate = monday)

    val result = useCase(weekData, range, today = monday)

    assertEquals(0, result.completed)
    assertEquals(0, result.total)
    assertEquals(0f, result.rate)
  }

  @Test
  fun `when config created mid-week then calculates from config start date`() {
    val monday = LocalDate(2024, 1, 8)
    val wednesday = LocalDate(2024, 1, 10)
    val thursday = LocalDate(2024, 1, 11)
    val weekData = createWeekData(
      monday to dayWithHabits(monday, completed = 0, total = 3),
      LocalDate(2024, 1, 9) to dayWithHabits(LocalDate(2024, 1, 9), completed = 0, total = 3),
      wednesday to dayWithHabits(wednesday, completed = 3, total = 3),
      thursday to dayWithHabits(thursday, completed = 2, total = 3)
    )
    val range = ActivityRange.Week(startDate = monday)

    val result = useCase(weekData, range, today = thursday, configStartDate = wednesday)

    // Should only count Wednesday (3/3) and Thursday (2/3), not Monday and Tuesday
    assertEquals(5, result.completed)
    assertEquals(6, result.total)
    assertEquals(0.833f, result.rate, 0.01f)
  }

  @Test
  fun `when config start date is before range then uses range start`() {
    val monday = LocalDate(2024, 1, 8)
    val lastWeek = LocalDate(2024, 1, 1)
    val thursday = LocalDate(2024, 1, 11)
    val weekData = createWeekData(
      monday to dayWithHabits(monday, completed = 2, total = 3),
      LocalDate(2024, 1, 9) to dayWithHabits(LocalDate(2024, 1, 9), completed = 3, total = 3),
      LocalDate(2024, 1, 10) to dayWithHabits(LocalDate(2024, 1, 10), completed = 1, total = 3),
      thursday to dayWithHabits(thursday, completed = 2, total = 3)
    )
    val range = ActivityRange.Week(startDate = monday)

    val result = useCase(weekData, range, today = thursday, configStartDate = lastWeek)

    // Config started before this week, so use full week range
    assertEquals(8, result.completed)
    assertEquals(12, result.total)
    assertEquals(0.667f, result.rate, 0.01f)
  }

  @Test
  fun `when past period with inRange false then still calculates correctly`() {
    val monday = LocalDate(2024, 1, 1)
    val weekData = createWeekData(
      monday to dayWithHabits(monday, completed = 2, total = 2, inRange = false),
      LocalDate(2024, 1, 2) to dayWithHabits(LocalDate(2024, 1, 2), completed = 1, total = 2, inRange = false),
      LocalDate(2024, 1, 3) to dayWithHabits(LocalDate(2024, 1, 3), completed = 2, total = 2, inRange = false)
    )
    val range = ActivityRange.Week(startDate = monday)
    val today = LocalDate(2024, 1, 15)

    val result = useCase(weekData, range, today)

    assertEquals(5, result.completed)
    assertEquals(6, result.total)
    assertEquals(0.833f, result.rate, 0.01f)
  }

  private fun dayWithHabits(
    date: LocalDate,
    completed: Int,
    total: Int,
    inRange: Boolean = true
  ): ContributionDay {
    return ContributionDay(
      date = date,
      count = completed,
      totalHabits = total,
      completionRatio = if (total > 0) completed.toFloat() / total else 0f,
      habitStatuses = emptyList(),
      dailyMemo = null,
      inRange = inRange
    )
  }

  private fun createWeekData(vararg days: Pair<LocalDate, ContributionDay>): ActivityWeekData {
    val sortedDays = days.sortedBy { it.first }
    val weeks = sortedDays.chunked(7).map { chunk ->
      ActivityWeek(
        startDate = chunk.first().first,
        days = chunk.map { it.second },
        weeklyCount = chunk.sumOf { it.second.count }
      )
    }
    val maxDaily = days.maxOfOrNull { it.second.count } ?: 0
    val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
    return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
  }
}
