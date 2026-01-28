package space.be1ski.vibits.shared.feature.habits.presentation

import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange

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

  // Activity data recalculation - NEW
  data class RecalculateActivityData(
    val range: ActivityRange,
    val mode: ActivityMode,
    val memos: List<space.be1ski.vibits.shared.feature.memos.domain.model.Memo>,
  ) : HabitsEffect
}
