package space.be1ski.memos.shared.presentation.app

internal enum class MemosFabMode {
  Habits,
  Memo
}

internal fun memosFabModeForTab(selectedTab: Int): MemosFabMode =
  if (selectedTab == 0) MemosFabMode.Habits else MemosFabMode.Memo
