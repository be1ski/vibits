package space.be1ski.vibits.shared.feature.habits.presentation

import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Actions for the Habits feature.
 */
sealed interface HabitsAction {
  // Editor lifecycle
  data class OpenEditor(
    val day: ContributionDay? = null,
    val memo: Memo? = null,
    val config: List<HabitConfig>,
  ) : HabitsAction

  data object CloseEditor : HabitsAction

  // Editor interactions
  data class ToggleHabit(
    val tag: String,
    val checked: Boolean,
  ) : HabitsAction

  data object ConfirmEditor : HabitsAction

  data object RequestDelete : HabitsAction

  data object ConfirmDelete : HabitsAction

  data object CancelDelete : HabitsAction

  // Config dialog
  data class OpenConfigDialog(
    val currentConfig: List<HabitConfig>,
    val existingMemo: Memo? = null,
  ) : HabitsAction

  data object CloseConfigDialog : HabitsAction

  data object AddHabit : HabitsAction

  data class UpdateHabitLabel(
    val id: String,
    val label: String,
  ) : HabitsAction

  data class UpdateHabitColor(
    val id: String,
    val color: Long,
  ) : HabitsAction

  data class DeleteHabit(
    val id: String,
  ) : HabitsAction

  data object SaveConfigDialog : HabitsAction

  data object RequestDeleteConfig : HabitsAction

  data object ConfirmDeleteConfig : HabitsAction

  data object CancelDeleteConfig : HabitsAction

  // Edit existing config warning
  data object DismissEditConfigWarning : HabitsAction

  data object ConfirmEditExistingConfig : HabitsAction

  data object CreateNewConfigInstead : HabitsAction

  // Single habit toggle (quick toggle from matrix)
  data class RequestSingleHabitToggle(
    val day: ContributionDay,
    val habitTag: String,
    val habitLabel: String,
    val config: List<HabitConfig>,
  ) : HabitsAction

  data object ConfirmSingleHabitToggle : HabitsAction

  data object CancelSingleHabitToggle : HabitsAction

  // Selection management
  data class SelectDay(
    val day: ContributionDay,
    val selectionId: String,
  ) : HabitsAction

  data class SelectWeek(
    val week: ActivityWeek,
  ) : HabitsAction

  data object ClearSelection : HabitsAction

  // API responses
  data class MemoCreated(
    val memo: Memo,
  ) : HabitsAction

  data class MemoUpdated(
    val memo: Memo,
  ) : HabitsAction

  data class MemoDeleted(
    val name: String,
  ) : HabitsAction

  data class MemoOperationFailed(
    val error: String,
  ) : HabitsAction

  // Cache management
  data class RequestPrewarmAllRanges(
    val memos: List<Memo>,
    val appMode: AppMode,
  ) : HabitsAction

  data class UpdateActivityData(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val weekData: ActivityWeekData,
    val configTimeline: List<HabitsConfigEntry>,
    val successRate: SuccessRateData?,
  ) : HabitsAction

  data object PrewarmCompleted : HabitsAction

  data object InvalidateAllCache : HabitsAction

  data class InvalidateCache(
    val range: ActivityRange,
    val mode: ActivityMode,
    val appMode: AppMode,
    val memos: List<Memo>,
  ) : HabitsAction
}
