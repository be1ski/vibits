package space.be1ski.vibits.feature.habits.presentation.effect

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig

/**
 * Side effects for the Habits feature.
 */
sealed interface HabitsEffect {
  sealed interface Memo : HabitsEffect

  sealed interface Refresh : HabitsEffect

  sealed interface Activity : HabitsEffect

  /**
   * Toggle a single habit for a specific date.
   * The use case will read current memo state, apply the toggle, and save.
   * This ensures atomic read-modify-write operations even with rapid toggles.
   */
  data class ToggleDailyHabit(
    val date: LocalDate,
    val habitTag: String,
    val habitsConfig: List<HabitConfig>,
  ) : Memo

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
    val memos: List<space.be1ski.vibits.feature.memos.domain.model.Memo>,
    val appMode: AppMode,
  ) : Activity

  data class RecalculateActivityData(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val memos: List<space.be1ski.vibits.feature.memos.domain.model.Memo>,
  ) : Activity
}
