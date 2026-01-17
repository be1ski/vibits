@file:Suppress("MatchingDeclarationName", "ktlint:standard:filename")

package space.be1ski.vibits.shared.app

internal enum class MemosFabMode {
  HABITS,
  MEMO,
}

internal fun memosFabModeForScreen(selectedScreen: MemosScreen): MemosFabMode =
  if (selectedScreen == MemosScreen.HABITS) MemosFabMode.HABITS else MemosFabMode.MEMO
