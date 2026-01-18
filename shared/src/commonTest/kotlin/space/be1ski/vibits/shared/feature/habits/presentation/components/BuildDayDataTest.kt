package space.be1ski.vibits.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildDayDataTest {
  private val today = LocalDate(2024, 1, 15)
  private val configDate = LocalDate(2024, 1, 10)
  private val habits = listOf(HabitConfig(tag = "#habits/test", label = "Test"))
  private val configMemo = Memo(name = "config", content = "#habits/config\nTest | #habits/test")
  private val configEntry = HabitsConfigEntry(date = configDate, habits = habits, memo = configMemo)
  private val bounds = RangeBounds(start = LocalDate(2024, 1, 1), end = LocalDate(2024, 1, 31))

  @Test
  fun `when date is today then isClickable is true`() {
    val context =
      DayDataContext(
        date = today,
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertTrue(result.isClickable)
  }

  @Test
  fun `when date is before today but after config then isClickable is true`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertTrue(result.isClickable)
  }

  @Test
  fun `when date is in the future then isClickable is false`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 20),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertFalse(result.isClickable)
  }

  @Test
  fun `when date is before config start date then isClickable is false`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 5),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertFalse(result.isClickable)
  }

  @Test
  fun `when config timeline is empty then isClickable is true`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.POSTS,
        configTimeline = emptyList(),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertTrue(result.isClickable)
  }

  @Test
  fun `when date equals config start date then isClickable is true`() {
    val context =
      DayDataContext(
        date = configDate,
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertTrue(result.isClickable)
  }

  // inRange tests

  @Test
  fun `when date is within bounds then inRange is true`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 15),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertTrue(result.inRange)
  }

  @Test
  fun `when date is before bounds start then inRange is false`() {
    val context =
      DayDataContext(
        date = LocalDate(2023, 12, 31),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertFalse(result.inRange)
  }

  @Test
  fun `when date is after bounds end then inRange is false`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 2, 1),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertFalse(result.inRange)
  }

  @Test
  fun `when date equals bounds start then inRange is true`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 1),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertTrue(result.inRange)
  }

  @Test
  fun `when date equals bounds end then inRange is true`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 31),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertTrue(result.inRange)
  }

  // count and completionRatio tests for HABITS mode

  @Test
  fun `when habit is completed in dailyMemo then count is 1`() {
    val dailyMemo = DailyMemoInfo(name = "daily", content = "#habits/test done!")
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = mapOf(LocalDate(2024, 1, 12) to dailyMemo),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertEquals(1, result.count)
    assertEquals(1f, result.completionRatio)
  }

  @Test
  fun `when no dailyMemo for date then count is 0`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertEquals(0, result.count)
    assertEquals(0f, result.completionRatio)
  }

  @Test
  fun `when habit tag not in dailyMemo then count is 0`() {
    val dailyMemo = DailyMemoInfo(name = "daily", content = "just some text without habits")
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(configEntry),
        dailyMemos = mapOf(LocalDate(2024, 1, 12) to dailyMemo),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertEquals(0, result.count)
  }

  @Test
  fun `when multiple habits and some completed then count reflects completed`() {
    val multiHabits =
      listOf(
        HabitConfig(tag = "#habits/exercise", label = "Exercise"),
        HabitConfig(tag = "#habits/reading", label = "Reading"),
        HabitConfig(tag = "#habits/meditation", label = "Meditation"),
      )
    val multiConfigEntry =
      HabitsConfigEntry(
        date = configDate,
        habits = multiHabits,
        memo =
          Memo(
            name = "config",
            content =
              "#habits/config\n" +
                "Exercise | #habits/exercise\n" +
                "Reading | #habits/reading\n" +
                "Meditation | #habits/meditation",
          ),
      )
    val dailyMemo = DailyMemoInfo(name = "daily", content = "#habits/exercise #habits/reading")
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.HABITS,
        configTimeline = listOf(multiConfigEntry),
        dailyMemos = mapOf(LocalDate(2024, 1, 12) to dailyMemo),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertEquals(2, result.count)
    assertEquals(3, result.totalHabits)
    assertEquals(2f / 3f, result.completionRatio, 0.01f)
  }

  // count tests for POSTS mode

  @Test
  fun `when mode is POSTS then count comes from counts map`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.POSTS,
        configTimeline = emptyList(),
        dailyMemos = emptyMap(),
        counts = mapOf(LocalDate(2024, 1, 12) to 5),
        today = today,
      )

    val result = buildDayData(context)

    assertEquals(5, result.count)
  }

  @Test
  fun `when mode is POSTS and no count for date then count is 0`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.POSTS,
        configTimeline = emptyList(),
        dailyMemos = emptyMap(),
        counts = emptyMap(),
        today = today,
      )

    val result = buildDayData(context)

    assertEquals(0, result.count)
  }

  @Test
  fun `when mode is POSTS then totalHabits is 0`() {
    val context =
      DayDataContext(
        date = LocalDate(2024, 1, 12),
        bounds = bounds,
        mode = ActivityMode.POSTS,
        configTimeline = emptyList(),
        dailyMemos = emptyMap(),
        counts = mapOf(LocalDate(2024, 1, 12) to 3),
        today = today,
      )

    val result = buildDayData(context)

    assertEquals(0, result.totalHabits)
  }
}
