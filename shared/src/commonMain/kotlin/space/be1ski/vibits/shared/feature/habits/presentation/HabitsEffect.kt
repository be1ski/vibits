package space.be1ski.vibits.shared.feature.habits.presentation

import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Side effects for the Habits feature.
 */
sealed interface HabitsEffect {
  sealed interface Memo : HabitsEffect

  sealed interface Refresh : HabitsEffect

  sealed interface Activity : HabitsEffect

  data class CreateMemo(
    val content: String,
  ) : Memo

  data class UpdateMemo(
    val name: String,
    val content: String,
  ) : Memo

  data class DeleteMemo(
    val name: String,
  ) : Memo

  data object RefreshMemos : Refresh

  data class RunPrewarmAllRanges(
    val memos: List<space.be1ski.vibits.shared.feature.memos.domain.model.Memo>,
    val appMode: AppMode,
  ) : Activity

  data class RecalculateActivityData(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val memos: List<space.be1ski.vibits.shared.feature.memos.domain.model.Memo>,
  ) : Activity
}
