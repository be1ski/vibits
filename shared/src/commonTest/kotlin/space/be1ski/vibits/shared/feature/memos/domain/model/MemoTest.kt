package space.be1ski.vibits.shared.feature.memos.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemoTest {
  @Test
  fun `when memo has habits config tag then postType is CONFIG`() {
    val memo = Memo(content = "#habits/config\nExercise | #habits/exercise")

    assertEquals(PostFilter.CONFIG, memo.postType)
  }

  @Test
  fun `when memo has habits config alt tag then postType is CONFIG`() {
    val memo = Memo(content = "#habits_config\nExercise | #habits/exercise")

    assertEquals(PostFilter.CONFIG, memo.postType)
  }

  @Test
  fun `when memo has habits daily tag then postType is HABIT_TRACKING`() {
    val memo = Memo(content = "#habits/daily 2024-01-15\n#habits/exercise")

    assertEquals(PostFilter.HABIT_TRACKING, memo.postType)
  }

  @Test
  fun `when memo has daily tag then postType is HABIT_TRACKING`() {
    val memo = Memo(content = "#daily 2024-01-15\n#habits/exercise")

    assertEquals(PostFilter.HABIT_TRACKING, memo.postType)
  }

  @Test
  fun `when memo has no special tags then postType is REGULAR`() {
    val memo = Memo(content = "Regular memo content")

    assertEquals(PostFilter.REGULAR, memo.postType)
  }

  @Test
  fun `when memo is config post then isConfigPost is true`() {
    val memo = Memo(content = "#habits/config\nExercise | #habits/exercise")

    assertTrue(memo.isConfigPost)
    assertFalse(memo.isTrackingPost)
    assertFalse(memo.isRegularPost)
  }

  @Test
  fun `when memo is tracking post then isTrackingPost is true`() {
    val memo = Memo(content = "#habits/daily 2024-01-15\n#habits/exercise")

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

  @Test
  fun `when memo is regular post then canDeleteFromFeed is true`() {
    val memo = Memo(content = "Regular memo content")

    assertTrue(memo.canDeleteFromFeed)
  }

  @Test
  fun `when memo is config post then canDeleteFromFeed is false`() {
    val memo = Memo(content = "#habits/config\nExercise | #habits/exercise")

    assertFalse(memo.canDeleteFromFeed)
  }

  @Test
  fun `when memo is tracking post then canDeleteFromFeed is false`() {
    val memo = Memo(content = "#habits/daily 2024-01-15\n#habits/exercise")

    assertFalse(memo.canDeleteFromFeed)
  }
}
