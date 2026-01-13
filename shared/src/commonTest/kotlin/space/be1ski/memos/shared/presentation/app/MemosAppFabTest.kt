package space.be1ski.memos.shared.presentation.app

import kotlin.test.Test
import kotlin.test.assertEquals

class MemosAppFabTest {
  @Test
  fun `when habits tab selected then fab mode is habits`() {
    val habitsTab = 0
    val result = memosFabModeForTab(habitsTab)
    assertEquals(MemosFabMode.Habits, result)
  }

  @Test
  fun `when non habits tab selected then fab mode is memo`() {
    val statsTab = 1
    val feedTab = 2
    val statsResult = memosFabModeForTab(statsTab)
    val feedResult = memosFabModeForTab(feedTab)
    assertEquals(MemosFabMode.Memo, statsResult)
    assertEquals(MemosFabMode.Memo, feedResult)
  }
}
