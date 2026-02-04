package space.be1ski.vibits.feature.memos.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PostTagsExtensionsTest {
  @Test
  fun `isConfigMemo when contains HABITS_CONFIG then returns true`() {
    val content = "Some text\n#habits/config\nMore text"

    assertTrue(content.isConfigMemo())
  }

  @Test
  fun `isConfigMemo when contains HABITS_CONFIG_ALT then returns true`() {
    val content = "Some text\n#habits_config\nMore text"

    assertTrue(content.isConfigMemo())
  }

  @Test
  fun `isConfigMemo when no config tag then returns false`() {
    val content = "#habits/exercise"

    assertFalse(content.isConfigMemo())
  }

  @Test
  fun `isDailyMemo when contains HABITS_DAILY then returns true`() {
    val content = "#habits/daily 2024-01-15"

    assertTrue(content.isDailyMemo())
  }

  @Test
  fun `isDailyMemo when contains DAILY then returns true`() {
    val content = "#daily 2024-01-15"

    assertTrue(content.isDailyMemo())
  }

  @Test
  fun `isDailyMemo when no daily tag then returns false`() {
    val content = "#habits/exercise"

    assertFalse(content.isDailyMemo())
  }

  @Test
  fun `isConfigTag when starts with HABITS_CONFIG then returns true`() {
    val tag = "#habits/config"

    assertTrue(tag.isConfigTag())
  }

  @Test
  fun `isConfigTag when starts with HABITS_CONFIG_ALT then returns true`() {
    val tag = "#habits_config"

    assertTrue(tag.isConfigTag())
  }

  @Test
  fun `isConfigTag when does not start with config prefix then returns false`() {
    val tag = "#habits/exercise"

    assertFalse(tag.isConfigTag())
  }

  @Test
  fun `stripHabitPrefixes when has HABITS_PREFIX then removes it`() {
    val tag = "#habits/exercise"

    val result = tag.stripHabitPrefixes()

    kotlin.test.assertEquals("exercise", result)
  }

  @Test
  fun `stripHabitPrefixes when has HABIT_PREFIX then removes it`() {
    val tag = "#habit/reading"

    val result = tag.stripHabitPrefixes()

    kotlin.test.assertEquals("reading", result)
  }

  @Test
  fun `stripHabitPrefixes when has no prefix then returns original`() {
    val tag = "exercise"

    val result = tag.stripHabitPrefixes()

    kotlin.test.assertEquals("exercise", result)
  }
}
