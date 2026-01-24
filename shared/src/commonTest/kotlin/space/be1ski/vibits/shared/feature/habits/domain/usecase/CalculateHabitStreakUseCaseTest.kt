package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CalculateHabitStreakUseCaseTest {
  private val calculateStreakUseCase = CalculateStreakUseCase()
  private val useCase = CalculateHabitStreakUseCase(calculateStreakUseCase)

  private val habitGym = HabitConfig(tag = "#habits/gym", label = "Gym")
  private val habitReading = HabitConfig(tag = "#habits/reading", label = "Reading")

  @Test
  fun `when single habit completed then current and best are 1`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekDataWithHabits(
        today to
          dayWithMultipleHabits(
            today,
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
            ),
          ),
      )

    val result = useCase(weekData, habitGym, today)

    assertEquals(habitGym.tag, result.habitTag)
    assertEquals(1, result.current)
    assertEquals(1, result.best)
    assertEquals(today, result.currentStreakStart)
  }

  @Test
  fun `when habit completed consecutively then calculates streak`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekDataWithHabits(
        LocalDate(2024, 1, 13) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 13),
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
            ),
          ),
        LocalDate(2024, 1, 14) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 14),
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
            ),
          ),
        today to
          dayWithMultipleHabits(
            today,
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
            ),
          ),
      )

    val result = useCase(weekData, habitGym, today)

    assertEquals(3, result.current)
    assertEquals(3, result.best)
    assertEquals(LocalDate(2024, 1, 13), result.currentStreakStart)
  }

  @Test
  fun `when habit not completed today then current streak is zero`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekDataWithHabits(
        LocalDate(2024, 1, 13) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 13),
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
            ),
          ),
        LocalDate(2024, 1, 14) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 14),
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
            ),
          ),
        today to
          dayWithMultipleHabits(
            today,
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = false),
            ),
          ),
      )

    val result = useCase(weekData, habitGym, today)

    assertEquals(0, result.current)
    assertEquals(2, result.best)
    assertNull(result.currentStreakStart)
  }

  @Test
  fun `when tracking multiple habits then calculates per-habit streak independently`() {
    val today = LocalDate(2024, 1, 15)
    val weekData =
      createWeekDataWithHabits(
        LocalDate(2024, 1, 13) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 13),
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
              HabitStatus(tag = habitReading.tag, label = habitReading.label, done = true),
            ),
          ),
        LocalDate(2024, 1, 14) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 14),
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = false),
              HabitStatus(tag = habitReading.tag, label = habitReading.label, done = true),
            ),
          ),
        today to
          dayWithMultipleHabits(
            today,
            listOf(
              HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true),
              HabitStatus(tag = habitReading.tag, label = habitReading.label, done = true),
            ),
          ),
      )

    val gymResult = useCase(weekData, habitGym, today)
    val readingResult = useCase(weekData, habitReading, today)

    // Gym: broke on 14th, current = 1
    assertEquals(1, gymResult.current)
    assertEquals(1, gymResult.best)
    assertEquals(today, gymResult.currentStreakStart)

    // Reading: unbroken, current = 3
    assertEquals(3, readingResult.current)
    assertEquals(3, readingResult.best)
    assertEquals(LocalDate(2024, 1, 13), readingResult.currentStreakStart)
  }

  @Test
  fun `when habit has best streak in past then tracks it`() {
    val today = LocalDate(2024, 1, 20)
    val weekData =
      createWeekDataWithHabits(
        LocalDate(2024, 1, 10) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 10),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 11) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 11),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 12) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 12),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 13) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 13),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        // Gap
        LocalDate(2024, 1, 14) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 14),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = false)),
          ),
        // New shorter streak
        LocalDate(2024, 1, 19) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 19),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        today to
          dayWithMultipleHabits(
            today,
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
      )

    val result = useCase(weekData, habitGym, today)

    assertEquals(2, result.current)
    assertEquals(4, result.best)
    assertEquals(LocalDate(2024, 1, 19), result.currentStreakStart)
  }

  @Test
  fun `when habit added mid-timeline then ignores earlier days`() {
    val today = LocalDate(2024, 1, 20)
    val configStartDate = LocalDate(2024, 1, 15)
    val weekData =
      createWeekDataWithHabits(
        LocalDate(2024, 1, 10) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 10),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 11) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 11),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        // Config starts here
        configStartDate to
          dayWithMultipleHabits(
            configStartDate,
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 16) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 16),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 17) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 17),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 18) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 18),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        LocalDate(2024, 1, 19) to
          dayWithMultipleHabits(
            LocalDate(2024, 1, 19),
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
        today to
          dayWithMultipleHabits(
            today,
            listOf(HabitStatus(tag = habitGym.tag, label = habitGym.label, done = true)),
          ),
      )

    val result = useCase(weekData, habitGym, today, configStartDate)

    // Should ignore days before configStartDate
    assertEquals(6, result.current)
    assertEquals(6, result.best)
    assertEquals(configStartDate, result.currentStreakStart)
  }

  private fun dayWithMultipleHabits(
    date: LocalDate,
    habitStatuses: List<HabitStatus>,
  ): ContributionDay {
    val totalHabits = habitStatuses.size
    val count = habitStatuses.count { it.done }
    return ContributionDay(
      date = date,
      count = count,
      totalHabits = totalHabits,
      completionRatio = if (totalHabits > 0) count.toFloat() / totalHabits else 0f,
      habitStatuses = habitStatuses,
      dailyMemo = null,
      inRange = true,
    )
  }

  private fun createWeekDataWithHabits(vararg days: Pair<LocalDate, ContributionDay>): ActivityWeekData {
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
