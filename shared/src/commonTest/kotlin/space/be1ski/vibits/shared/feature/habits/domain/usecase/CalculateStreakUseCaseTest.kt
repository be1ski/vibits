package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalculateStreakUseCaseTest {
  private val useCase = CalculateStreakUseCase()

  @Test
  fun `when no data then returns zero streaks`() {
    val today = LocalDate(2024, 1, 15)
    val weekData = createWeekData()

    val result = useCase(weekData, today)

    assertEquals(0, result.current)
    assertEquals(0, result.best)
    assertNull(result.currentStreakStart)
  }

  @Test
  fun `when single day with completion then current and best are 1`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekData(
        today to dayWithHabits(today, count = 1, totalHabits = 2),
      )

    val result = useCase(weekData, today)

    assertEquals(1, result.current)
    assertEquals(1, result.best)
    assertEquals(today, result.currentStreakStart)
  }

  @Test
  fun `when consecutive days then calculates current streak`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), count = 2, totalHabits = 3),
        LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), count = 1, totalHabits = 3),
        today to dayWithHabits(today, count = 3, totalHabits = 3),
      )

    val result = useCase(weekData, today)

    assertEquals(3, result.current)
    assertEquals(3, result.best)
    assertEquals(LocalDate(2024, 1, 13), result.currentStreakStart)
  }

  @Test
  fun `when gap in completions then breaks streak`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 10) to dayWithHabits(LocalDate(2024, 1, 10), count = 2, totalHabits = 3),
        LocalDate(2024, 1, 11) to dayWithHabits(LocalDate(2024, 1, 11), count = 2, totalHabits = 3),
        LocalDate(2024, 1, 12) to dayWithHabits(LocalDate(2024, 1, 12), count = 0, totalHabits = 3),
        LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), count = 1, totalHabits = 3),
        LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), count = 2, totalHabits = 3),
        today to dayWithHabits(today, count = 3, totalHabits = 3),
      )

    val result = useCase(weekData, today)

    assertEquals(3, result.current)
    assertEquals(3, result.best)
    assertEquals(LocalDate(2024, 1, 13), result.currentStreakStart)
  }

  @Test
  fun `when current less than historical then best is historical`() {
    val today = LocalDate(2024, 1, 20)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 10) to dayWithHabits(LocalDate(2024, 1, 10), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 11) to dayWithHabits(LocalDate(2024, 1, 11), count = 2, totalHabits = 2),
        LocalDate(2024, 1, 12) to dayWithHabits(LocalDate(2024, 1, 12), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), count = 2, totalHabits = 2),
        LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), count = 1, totalHabits = 2),
        // Gap - streak broken
        LocalDate(2024, 1, 15) to dayWithHabits(LocalDate(2024, 1, 15), count = 0, totalHabits = 2),
        // New streak (shorter)
        LocalDate(2024, 1, 19) to dayWithHabits(LocalDate(2024, 1, 19), count = 1, totalHabits = 2),
        today to dayWithHabits(today, count = 2, totalHabits = 2),
      )

    val result = useCase(weekData, today)

    assertEquals(2, result.current)
    assertEquals(5, result.best)
    assertEquals(LocalDate(2024, 1, 19), result.currentStreakStart)
  }

  @Test
  fun `when today has no completion then current streak is zero`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), count = 2, totalHabits = 3),
        LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), count = 1, totalHabits = 3),
        today to dayWithHabits(today, count = 0, totalHabits = 3),
      )

    val result = useCase(weekData, today)

    assertEquals(0, result.current)
    assertEquals(2, result.best)
    assertNull(result.currentStreakStart)
  }

  @Test
  fun `when config start date provided then ignores earlier data`() {
    val today = LocalDate(2024, 1, 20)
    val configStartDate = LocalDate(2024, 1, 15)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 10) to dayWithHabits(LocalDate(2024, 1, 10), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 11) to dayWithHabits(LocalDate(2024, 1, 11), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 12) to dayWithHabits(LocalDate(2024, 1, 12), count = 1, totalHabits = 2),
        // Config starts here
        configStartDate to dayWithHabits(configStartDate, count = 1, totalHabits = 3),
        LocalDate(2024, 1, 16) to dayWithHabits(LocalDate(2024, 1, 16), count = 2, totalHabits = 3),
        LocalDate(2024, 1, 17) to dayWithHabits(LocalDate(2024, 1, 17), count = 1, totalHabits = 3),
        LocalDate(2024, 1, 18) to dayWithHabits(LocalDate(2024, 1, 18), count = 3, totalHabits = 3),
        LocalDate(2024, 1, 19) to dayWithHabits(LocalDate(2024, 1, 19), count = 2, totalHabits = 3),
        today to dayWithHabits(today, count = 1, totalHabits = 3),
      )

    val result = useCase(weekData, today, configStartDate)

    // Should ignore days before configStartDate
    assertEquals(6, result.current)
    assertEquals(6, result.best)
    assertEquals(configStartDate, result.currentStreakStart)
  }

  @Test
  fun `when partial completions then counts as streak`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), count = 1, totalHabits = 3),
        LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), count = 2, totalHabits = 3),
        today to dayWithHabits(today, count = 1, totalHabits = 3),
      )

    val result = useCase(weekData, today)

    // Partial completions (not all habits done) should still count
    assertEquals(3, result.current)
    assertEquals(3, result.best)
    assertEquals(LocalDate(2024, 1, 13), result.currentStreakStart)
  }

  @Test
  fun `when future dates exist then ignores them`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), count = 2, totalHabits = 3),
        LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), count = 1, totalHabits = 3),
        today to dayWithHabits(today, count = 3, totalHabits = 3),
        LocalDate(2024, 1, 16) to dayWithHabits(LocalDate(2024, 1, 16), count = 2, totalHabits = 3),
        LocalDate(2024, 1, 17) to dayWithHabits(LocalDate(2024, 1, 17), count = 2, totalHabits = 3),
      )

    val result = useCase(weekData, today)

    // Should ignore future dates (16th and 17th)
    assertEquals(3, result.current)
    assertEquals(3, result.best)
    assertEquals(LocalDate(2024, 1, 13), result.currentStreakStart)
  }

  @Test
  fun `when days without config then skips them`() {
    val today = LocalDate(2024, 1, 20)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 15) to dayWithHabits(LocalDate(2024, 1, 15), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 16) to dayWithHabits(LocalDate(2024, 1, 16), count = 0, totalHabits = 0),
        LocalDate(2024, 1, 17) to dayWithHabits(LocalDate(2024, 1, 17), count = 0, totalHabits = 0),
        LocalDate(2024, 1, 18) to dayWithHabits(LocalDate(2024, 1, 18), count = 2, totalHabits = 2),
        LocalDate(2024, 1, 19) to dayWithHabits(LocalDate(2024, 1, 19), count = 1, totalHabits = 2),
        today to dayWithHabits(today, count = 2, totalHabits = 2),
      )

    val result = useCase(weekData, today)

    // Days without config (totalHabits = 0) should be skipped, not break streak
    assertEquals(3, result.current)
    assertEquals(3, result.best)
    assertEquals(LocalDate(2024, 1, 18), result.currentStreakStart)
  }

  @Test
  fun `when streak continues across multiple weeks`() {
    val today = LocalDate(2024, 1, 20)
    val days =
      (10..20).map { day ->
        val date = LocalDate(2024, 1, day)
        date to dayWithHabits(date, count = 2, totalHabits = 3)
      }
    val weekData = createWeekData(*days.toTypedArray())

    val result = useCase(weekData, today)

    assertEquals(11, result.current)
    assertEquals(11, result.best)
    assertEquals(LocalDate(2024, 1, 10), result.currentStreakStart)
  }

  @Test
  fun `when multiple streak periods then tracks best correctly`() {
    val today = LocalDate(2024, 1, 25)
    val weekData =
      createWeekData(
        LocalDate(2024, 1, 10) to dayWithHabits(LocalDate(2024, 1, 10), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 11) to dayWithHabits(LocalDate(2024, 1, 11), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 12) to dayWithHabits(LocalDate(2024, 1, 12), count = 1, totalHabits = 2),
        // Gap
        LocalDate(2024, 1, 13) to dayWithHabits(LocalDate(2024, 1, 13), count = 0, totalHabits = 2),
        // New streak (longer)
        LocalDate(2024, 1, 14) to dayWithHabits(LocalDate(2024, 1, 14), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 15) to dayWithHabits(LocalDate(2024, 1, 15), count = 2, totalHabits = 2),
        LocalDate(2024, 1, 16) to dayWithHabits(LocalDate(2024, 1, 16), count = 1, totalHabits = 2),
        LocalDate(2024, 1, 17) to dayWithHabits(LocalDate(2024, 1, 17), count = 2, totalHabits = 2),
        LocalDate(2024, 1, 18) to dayWithHabits(LocalDate(2024, 1, 18), count = 1, totalHabits = 2),
        // Gap
        LocalDate(2024, 1, 19) to dayWithHabits(LocalDate(2024, 1, 19), count = 0, totalHabits = 2),
        // New streak (current, shorter)
        LocalDate(2024, 1, 24) to dayWithHabits(LocalDate(2024, 1, 24), count = 1, totalHabits = 2),
        today to dayWithHabits(today, count = 2, totalHabits = 2),
      )

    val result = useCase(weekData, today)

    assertEquals(2, result.current)
    assertEquals(5, result.best)
    assertEquals(LocalDate(2024, 1, 24), result.currentStreakStart)
  }

  private fun dayWithHabits(
    date: LocalDate,
    count: Int,
    totalHabits: Int,
  ): ContributionDay =
    ContributionDay(
      date = date,
      count = count,
      totalHabits = totalHabits,
      completionRatio = if (totalHabits > 0) count.toFloat() / totalHabits else 0f,
      habitStatuses = emptyList(),
      dailyMemo = null,
      inRange = true,
    )

  private fun createWeekData(vararg days: Pair<LocalDate, ContributionDay>): ActivityWeekData {
    if (days.isEmpty()) {
      return ActivityWeekData(weeks = emptyList(), maxDaily = 0, maxWeekly = 0)
    }
    val sortedDays = days.sortedBy { it.first }
    val weeks =
      sortedDays.chunked(7).map { chunk ->
        ActivityWeek(
          startDate = chunk.first().first,
          days = chunk.map { it.second },
          weeklyCount = chunk.sumOf { it.second.count },
        )
      }
    val maxDaily = days.maxOfOrNull { it.second.count } ?: 0
    val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
    return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
  }
}
