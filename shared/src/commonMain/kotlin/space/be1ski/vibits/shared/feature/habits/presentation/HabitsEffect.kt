package space.be1ski.vibits.shared.feature.habits.presentation

import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Side effects for the Habits feature.
 */
sealed interface HabitsEffect {
  data class CreateMemo(
    val content: String,
  ) : HabitsEffect

  data class UpdateMemo(
    val name: String,
    val content: String,
  ) : HabitsEffect

  data class DeleteMemo(
    val name: String,
  ) : HabitsEffect

  data object RefreshMemos : HabitsEffect

  data class RunPrewarmAllRanges(
    val memos: List<Memo>,
    val appMode: AppMode,
  ) : HabitsEffect

  data class RecalculateActivityData(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val memos: List<Memo>,
  ) : HabitsEffect
}
