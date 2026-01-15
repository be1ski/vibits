package space.be1ski.vibits.shared.domain.habits

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsConfigContentFromList
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsEditorSelections
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus

class HabitContentBuilderTest {

  // buildDailyContent tests

  @Test
  fun `when selections have done habits then includes them`() {
    val date = LocalDate(2024, 1, 15)
    val config = listOf(
      HabitConfig("#habits/exercise", "Exercise"),
      HabitConfig("#habits/reading", "Reading")
    )
    val selections = mapOf(
      "#habits/exercise" to true,
      "#habits/reading" to false
    )

    val result = buildDailyContent(date, config, selections)

    assertTrue(result.contains("#habits/daily 2024-01-15"))
    assertTrue(result.contains("#habits/exercise"))
    assertTrue(!result.contains("#habits/reading"))
  }

  @Test
  fun `when no habits selected then only has header`() {
    val date = LocalDate(2024, 1, 15)
    val config = listOf(HabitConfig("#habits/exercise", "Exercise"))
    val selections = mapOf("#habits/exercise" to false)

    val result = buildDailyContent(date, config, selections)

    assertEquals("#habits/daily 2024-01-15\n\n", result)
  }

  @Test
  fun `when all habits selected then includes all`() {
    val date = LocalDate(2024, 1, 15)
    val config = listOf(
      HabitConfig("#habits/exercise", "Exercise"),
      HabitConfig("#habits/reading", "Reading")
    )
    val selections = mapOf(
      "#habits/exercise" to true,
      "#habits/reading" to true
    )

    val result = buildDailyContent(date, config, selections)

    assertTrue(result.contains("#habits/exercise"))
    assertTrue(result.contains("#habits/reading"))
  }

  // buildHabitsEditorSelections tests

  @Test
  fun `when day has habit statuses then builds selections from them`() {
    val day = ContributionDay(
      date = LocalDate(2024, 1, 15),
      count = 1,
      totalHabits = 2,
      completionRatio = 0.5f,
      habitStatuses = listOf(
        HabitStatus("#habits/exercise", "Exercise", done = true),
        HabitStatus("#habits/reading", "Reading", done = false)
      ),
      dailyMemo = null,
      inRange = true
    )
    val config = listOf(
      HabitConfig("#habits/exercise", "Exercise"),
      HabitConfig("#habits/reading", "Reading")
    )

    val result = buildHabitsEditorSelections(day, config)

    assertEquals(true, result["#habits/exercise"])
    assertEquals(false, result["#habits/reading"])
  }

  @Test
  fun `when config is empty then uses day statuses`() {
    val day = ContributionDay(
      date = LocalDate(2024, 1, 15),
      count = 1,
      totalHabits = 1,
      completionRatio = 1f,
      habitStatuses = listOf(
        HabitStatus("#habits/exercise", "Exercise", done = true)
      ),
      dailyMemo = null,
      inRange = true
    )

    val result = buildHabitsEditorSelections(day, emptyList())

    assertEquals(true, result["#habits/exercise"])
  }

  @Test
  fun `when config has habit not in day then defaults to false`() {
    val day = ContributionDay(
      date = LocalDate(2024, 1, 15),
      count = 0,
      totalHabits = 0,
      completionRatio = 0f,
      habitStatuses = emptyList(),
      dailyMemo = null,
      inRange = true
    )
    val config = listOf(HabitConfig("#habits/new_habit", "New Habit"))

    val result = buildHabitsEditorSelections(day, config)

    assertEquals(false, result["#habits/new_habit"])
  }

  // buildHabitsConfigContentFromList tests

  @Test
  fun `when entries exist then builds config content with header`() {
    val entries = listOf(
      HabitConfig("#habits/exercise", "Exercise", 0xFF4CAF50L),
      HabitConfig("#habits/reading", "Reading", 0xFF2196F3L)
    )

    val result = buildHabitsConfigContentFromList(entries)

    assertTrue(result.startsWith("#habits/config\n\n"))
    assertTrue(result.contains("Exercise | #habits/exercise | #4CAF50"))
    assertTrue(result.contains("Reading | #habits/reading | #2196F3"))
  }

  @Test
  fun `when entries empty then returns only header`() {
    val result = buildHabitsConfigContentFromList(emptyList())

    assertEquals("#habits/config\n\n", result)
  }
}
