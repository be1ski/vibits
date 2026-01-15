@file:Suppress("MatchingDeclarationName")

package space.be1ski.vibits.shared.app

internal enum class MemosFabMode {
  Habits,
  Memo
}

internal fun memosFabModeForScreen(selectedScreen: MemosScreen): MemosFabMode =
  if (selectedScreen == MemosScreen.Habits) MemosFabMode.Habits else MemosFabMode.Memo
