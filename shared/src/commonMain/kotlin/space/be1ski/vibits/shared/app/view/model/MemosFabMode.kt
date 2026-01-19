package space.be1ski.vibits.shared.app.view.model

internal enum class MemosFabMode {
  HABITS,
  MEMO,
}

internal fun memosFabModeForScreen(selectedScreen: MemosScreen): MemosFabMode =
  if (selectedScreen == MemosScreen.HABITS) MemosFabMode.HABITS else MemosFabMode.MEMO
