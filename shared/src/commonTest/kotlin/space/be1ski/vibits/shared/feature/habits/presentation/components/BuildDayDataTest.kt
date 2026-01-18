package space.be1ski.vibits.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
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
}
