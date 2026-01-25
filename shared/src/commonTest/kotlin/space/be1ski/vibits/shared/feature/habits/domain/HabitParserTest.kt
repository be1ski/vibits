package space.be1ski.vibits.shared.feature.habits.domain

import space.be1ski.vibits.shared.feature.habits.domain.buildHabitStatuses
import space.be1ski.vibits.shared.feature.habits.domain.extractCompletedHabits
import space.be1ski.vibits.shared.feature.habits.domain.extractHabitTagsFromContent
import space.be1ski.vibits.shared.feature.habits.domain.formatHexColor
import space.be1ski.vibits.shared.feature.habits.domain.labelFromTag
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.normalizeHabitTag
import space.be1ski.vibits.shared.feature.habits.domain.parseConfigFromContent
import space.be1ski.vibits.shared.feature.habits.domain.parseHabitConfigLine
import space.be1ski.vibits.shared.feature.habits.domain.parseHexColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitParserTest {
  // parseHabitConfigLine tests

  @Test
  fun `when line has label and tag then parses both`() {
    val result = parseHabitConfigLine("Exercise | #habits/exercise")
    assertEquals("Exercise", result?.label)
    assertEquals("#habits/exercise", result?.tag)
  }

  @Test
  fun `when line has only tag then derives label`() {
    val result = parseHabitConfigLine("#habits/morning_workout")
    assertEquals("morning workout", result?.label)
    assertEquals("#habits/morning_workout", result?.tag)
  }

  @Test
  fun `when line has only label then derives tag`() {
    val result = parseHabitConfigLine("Morning Run")
    assertEquals("Morning Run", result?.label)
    assertEquals("#habits/Morning_Run", result?.tag)
  }

  @Test
  fun `when line is blank then returns null`() {
    assertNull(parseHabitConfigLine(""))
    assertNull(parseHabitConfigLine("   "))
  }

  @Test
  fun `when line has extra whitespace then trims`() {
    val result = parseHabitConfigLine("  Exercise  |  #habits/exercise  ")
    assertEquals("Exercise", result?.label)
  }

  @Test
  fun `when line has label tag and color then parses all three`() {
    val result = parseHabitConfigLine("Exercise | #habits/exercise | #4CAF50")
    assertEquals("Exercise", result?.label)
    assertEquals("#habits/exercise", result?.tag)
    assertEquals(0xFF4CAF50L, result?.color)
  }

  @Test
  fun `when line has label tag and color with spaces then parses correctly`() {
    val result = parseHabitConfigLine("  Morning Run  |  #habits/morning_run  |  #FF5733  ")
    assertEquals("Morning Run", result?.label)
    assertEquals("#habits/morning_run", result?.tag)
    assertEquals(0xFFFF5733L, result?.color)
  }

  @Test
  fun `when line has tag and color without label then tag becomes label`() {
    // This format "#habits/tag | #color" is NOT supported -
    // parser treats first part as label, second as tag
    // This test documents current behavior to prevent regression
    val result = parseHabitConfigLine("#habits/exercise | #4CAF50")
    // Current behavior: #habits/exercise is treated as label, #4CAF50 as tag
    assertEquals("#habits/exercise", result?.label)
    assertEquals("#habits/#4CAF50", result?.tag) // Color becomes tag with #habits/ prefix
  }

  // normalizeHabitTag tests

  @Test
  fun `when tag has habits prefix then keeps it`() {
    assertEquals("#habits/exercise", normalizeHabitTag("#habits/exercise"))
  }

  @Test
  fun `when tag has habit prefix then normalizes to habits`() {
    assertEquals("#habits/exercise", normalizeHabitTag("#habit/exercise"))
  }

  @Test
  fun `when tag has no prefix then adds habits prefix`() {
    assertEquals("#habits/exercise", normalizeHabitTag("exercise"))
  }

  @Test
  fun `when tag has spaces then replaces with underscores`() {
    assertEquals("#habits/morning_run", normalizeHabitTag("morning run"))
  }

  @Test
  fun `when tag has multiple spaces then collapses to single underscore`() {
    assertEquals("#habits/morning_run", normalizeHabitTag("morning   run"))
  }

  @Test
  fun `when tag has cyrillic characters then preserves them`() {
    assertEquals("#habits/фывфывфывфыв", normalizeHabitTag("фывфывфывфыв"))
  }

  // labelFromTag tests

  @Test
  fun `when tag has habits prefix then removes it`() {
    assertEquals("exercise", labelFromTag("#habits/exercise"))
  }

  @Test
  fun `when tag has underscores then replaces with spaces`() {
    assertEquals("morning run", labelFromTag("#habits/morning_run"))
  }

  // extractCompletedHabits tests

  @Test
  fun `when content has checked checkboxes then extracts habits`() {
    val content =
      """
      - [x] Did #habits/exercise
      - [ ] #habits/reading
      - [X] Completed #habits/meditation
      """.trimIndent()
    val habits = setOf("#habits/exercise", "#habits/reading", "#habits/meditation")

    val result = extractCompletedHabits(content, habits)

    assertEquals(setOf("#habits/exercise", "#habits/meditation"), result)
  }

  @Test
  fun `when content has no checkboxes then extracts tags directly`() {
    val content =
      """
      #habits/daily 2024-01-15

      #habits/exercise
      #habits/meditation
      """.trimIndent()
    val habits = setOf("#habits/exercise", "#habits/meditation", "#habits/reading")

    val result = extractCompletedHabits(content, habits)

    assertEquals(setOf("#habits/exercise", "#habits/meditation"), result)
  }

  @Test
  fun `when checkbox format mixed with tags then prefers checkboxes`() {
    val content =
      """
      - [x] #habits/exercise
      #habits/reading
      """.trimIndent()
    val habits = setOf("#habits/exercise", "#habits/reading")

    val result = extractCompletedHabits(content, habits)

    // Should only find exercise because checkboxes take precedence
    assertEquals(setOf("#habits/exercise"), result)
  }

  @Test
  fun `when no habits match then returns empty set`() {
    val content = "#habits/unknown"
    val habits = setOf("#habits/exercise")

    val result = extractCompletedHabits(content, habits)

    assertTrue(result.isEmpty())
  }

  // extractHabitTagsFromContent tests

  @Test
  fun `when content has habit tags then extracts them`() {
    val content = "#habits/exercise #habits/meditation"

    val result = extractHabitTagsFromContent(content)

    assertEquals(setOf("#habits/exercise", "#habits/meditation"), result)
  }

  @Test
  fun `when content has daily tag then excludes it`() {
    val content = "#habits/daily 2024-01-15 #habits/exercise"

    val result = extractHabitTagsFromContent(content)

    assertEquals(setOf("#habits/exercise"), result)
  }

  @Test
  fun `when content is null then returns empty set`() {
    val result = extractHabitTagsFromContent(null)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `when content is blank then returns empty set`() {
    val result = extractHabitTagsFromContent("   ")
    assertTrue(result.isEmpty())
  }

  // buildHabitStatuses tests

  @Test
  fun `when content has completed habits then marks them done`() {
    val habits =
      listOf(
        HabitConfig("#habits/exercise", "Exercise"),
        HabitConfig("#habits/reading", "Reading"),
      )
    val content = "#habits/exercise"

    val result = buildHabitStatuses(content, habits)

    assertEquals(2, result.size)
    assertEquals(true, result.first { it.tag == "#habits/exercise" }.done)
    assertEquals(false, result.first { it.tag == "#habits/reading" }.done)
  }

  @Test
  fun `when content is null then all habits are not done`() {
    val habits = listOf(HabitConfig("#habits/exercise", "Exercise"))

    val result = buildHabitStatuses(null, habits)

    assertEquals(1, result.size)
    assertEquals(false, result.first().done)
  }

  @Test
  fun `when habits list is empty then returns empty list`() {
    val result = buildHabitStatuses("some content", emptyList())
    assertTrue(result.isEmpty())
  }

  // parseHexColor tests

  @Test
  fun `when hex color is valid 6 digits then parses with alpha`() {
    val result = parseHexColor("#FF5733")
    assertEquals(0xFFFF5733L, result) // Adds 0xFF alpha
  }

  @Test
  fun `when hex color without hash then parses with alpha`() {
    val result = parseHexColor("FF5733")
    assertEquals(0xFFFF5733L, result)
  }

  @Test
  fun `when hex color is invalid then returns null`() {
    assertNull(parseHexColor("invalid"))
    assertNull(parseHexColor("#GGG"))
  }

  @Test
  fun `when hex color is empty then returns null`() {
    assertNull(parseHexColor(""))
  }

  @Test
  fun `when hex color is 8 digits with alpha then parses correctly`() {
    val result = parseHexColor("#80FF5733")
    assertEquals(0x80FF5733L, result)
  }

  // formatHexColor tests

  @Test
  fun `when color is valid then formats with hash and uppercase`() {
    val result = formatHexColor(0xFF5733L)
    assertEquals("#FF5733", result)
  }

  @Test
  fun `when color has leading zeros then pads correctly`() {
    val result = formatHexColor(0x000033L)
    assertEquals("#000033", result)
  }

  // parseConfigFromContent tests

  @Test
  fun `when content is config memo then parses all habits`() {
    val content =
      """
      #habits/config
      Exercise | #habits/exercise | #4CAF50
      Reading | #habits/reading | #FF5733
      Meditation
      """.trimIndent()

    val result = parseConfigFromContent(content)

    assertEquals(3, result.size)
    assertEquals("Exercise", result[0].label)
    assertEquals("#habits/exercise", result[0].tag)
    assertEquals(0xFF4CAF50L, result[0].color)
    assertEquals("Reading", result[1].label)
    assertEquals("#habits/reading", result[1].tag)
    assertEquals(0xFFFF5733L, result[1].color)
    assertEquals("Meditation", result[2].label)
    assertEquals("#habits/Meditation", result[2].tag)
  }

  @Test
  fun `when content is config memo with alt tag then parses habits`() {
    val content =
      """
      #habits_config
      Exercise | #habits/exercise
      """.trimIndent()

    val result = parseConfigFromContent(content)

    assertEquals(1, result.size)
    assertEquals("Exercise", result[0].label)
    assertEquals("#habits/exercise", result[0].tag)
  }

  @Test
  fun `when content is not config memo then returns empty list`() {
    val content =
      """
      #habits/daily 2024-01-15
      #habits/exercise
      """.trimIndent()

    val result = parseConfigFromContent(content)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when content has duplicate tags then keeps only first occurrence`() {
    val content =
      """
      #habits/config
      Exercise | #habits/exercise | #4CAF50
      Different Label | #habits/exercise | #FF5733
      """.trimIndent()

    val result = parseConfigFromContent(content)

    assertEquals(1, result.size)
    assertEquals("Exercise", result[0].label)
    assertEquals("#habits/exercise", result[0].tag)
    assertEquals(0xFF4CAF50L, result[0].color)
  }

  @Test
  fun `when content has blank lines then ignores them`() {
    val content =
      """
      #habits/config

      Exercise | #habits/exercise

      Reading | #habits/reading
      """.trimIndent()

    val result = parseConfigFromContent(content)

    assertEquals(2, result.size)
  }
}
