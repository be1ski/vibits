package space.be1ski.memos.shared.domain.habits

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.HabitConfig
import space.be1ski.memos.shared.presentation.components.HabitStatus

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

  // buildHabitsConfigContent tests

  @Test
  fun `when raw text has valid lines then builds config content`() {
    val rawText = """
      Exercise | #habits/exercise
      Reading
    """.trimIndent()

    val result = buildHabitsConfigContent(rawText)

    assertTrue(result.startsWith("#habits/config"))
    assertTrue(result.contains("Exercise | #habits/exercise"))
    assertTrue(result.contains("Reading | #habits/Reading"))
  }

  @Test
  fun `when raw text is empty then only has header`() {
    val result = buildHabitsConfigContent("")

    assertEquals("#habits/config\n\n", result)
  }

  @Test
  fun `when raw text has blank lines then skips them`() {
    val rawText = """
      Exercise

      Reading
    """.trimIndent()

    val result = buildHabitsConfigContent(rawText)

    // Should have 2 entries, not 3
    val lines = result.lines().filter { it.contains("|") }
    assertEquals(2, lines.size)
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
}
