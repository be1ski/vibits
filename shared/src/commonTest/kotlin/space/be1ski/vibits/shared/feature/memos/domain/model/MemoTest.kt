package space.be1ski.vibits.shared.feature.memos.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemoTest {
  @Test
  fun `when memo has habits config tag then postType is CONFIG`() {
    val memo = Memo(content = "${PostTags.HABITS_CONFIG}\nExercise | #habits/exercise")

    assertEquals(PostFilter.CONFIG, memo.postType)
  }

  @Test
  fun `when memo has habits config alt tag then postType is CONFIG`() {
    val memo = Memo(content = "${PostTags.HABITS_CONFIG_ALT}\nExercise | #habits/exercise")

    assertEquals(PostFilter.CONFIG, memo.postType)
  }

  @Test
  fun `when memo has habits daily tag then postType is HABIT_TRACKING`() {
    val memo = Memo(content = "${PostTags.HABITS_DAILY} 2024-01-15\n#habits/exercise")

    assertEquals(PostFilter.HABIT_TRACKING, memo.postType)
  }

  @Test
  fun `when memo has daily tag then postType is HABIT_TRACKING`() {
    val memo = Memo(content = "${PostTags.DAILY} 2024-01-15\n#habits/exercise")

    assertEquals(PostFilter.HABIT_TRACKING, memo.postType)
  }

  @Test
  fun `when memo has no special tags then postType is REGULAR`() {
    val memo = Memo(content = "Regular memo content")

    assertEquals(PostFilter.REGULAR, memo.postType)
  }

  @Test
  fun `when memo is config post then isConfigPost is true`() {
    val memo = Memo(content = "${PostTags.HABITS_CONFIG}\nExercise | #habits/exercise")

    assertTrue(memo.isConfigPost)
    assertFalse(memo.isTrackingPost)
    assertFalse(memo.isRegularPost)
  }

  @Test
  fun `when memo is tracking post then isTrackingPost is true`() {
    val memo = Memo(content = "${PostTags.HABITS_DAILY} 2024-01-15\n#habits/exercise")

    assertFalse(memo.isConfigPost)
    assertTrue(memo.isTrackingPost)
    assertFalse(memo.isRegularPost)
  }

  @Test
  fun `when memo is regular post then isRegularPost is true`() {
    val memo = Memo(content = "Regular memo content")

    assertFalse(memo.isConfigPost)
    assertFalse(memo.isTrackingPost)
    assertTrue(memo.isRegularPost)
  }
}
