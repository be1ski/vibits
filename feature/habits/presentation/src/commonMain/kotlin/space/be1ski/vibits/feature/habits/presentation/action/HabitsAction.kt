package space.be1ski.vibits.feature.habits.presentation.action

import space.be1ski.vibits.core.elm.Action
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivitySummary
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.feature.habits.domain.model.SuccessRate
import space.be1ski.vibits.feature.memos.domain.model.Memo

/**
 * Actions for the Habits feature.
 */
sealed interface HabitsAction : Action {
  /**
   * Editor lifecycle and interactions.
   */
  sealed interface Editor : HabitsAction {
    data class OpenEditor(
      val day: ContributionDay? = null,
      val memo: Memo? = null,
      val config: List<HabitConfig>,
    ) : Editor

    data object CloseEditor : Editor

    data class ToggleHabit(
      val tag: String,
      val checked: Boolean,
    ) : Editor

    data object ConfirmEditor : Editor

    data object RequestDelete : Editor

    data object ConfirmDelete : Editor

    data object CancelDelete : Editor
  }

  /**
   * Config dialog management.
   */
  sealed interface Config : HabitsAction {
    data class OpenConfigDialog(
      val currentConfig: List<HabitConfig>,
      val existingMemo: Memo? = null,
    ) : Config

    data object CloseConfigDialog : Config

    data object AddHabit : Config

    data class UpdateHabitLabel(
      val id: String,
      val label: String,
    ) : Config

    data class UpdateHabitColor(
      val id: String,
      val color: Long,
    ) : Config

    data class DeleteHabit(
      val id: String,
    ) : Config

    data object SaveConfigDialog : Config
  }

  /**
   * Edit config warning flow.
   */
  sealed interface ConfigWarning : HabitsAction {
    data object DismissEditConfigWarning : ConfigWarning

    data object ConfirmEditExistingConfig : ConfigWarning

    data object CreateNewConfigInstead : ConfigWarning
  }

  /**
   * Delete config confirmation.
   */
  sealed interface ConfigDelete : HabitsAction {
    data object RequestDeleteConfig : ConfigDelete

    data object ConfirmDeleteConfig : ConfigDelete

    data object CancelDeleteConfig : ConfigDelete
  }

  /**
   * Single habit toggle from matrix.
   */
  sealed interface SingleToggle : HabitsAction {
    data class RequestSingleHabitToggle(
      val day: ContributionDay,
      val habitTag: String,
      val habitLabel: String,
      val config: List<HabitConfig>,
    ) : SingleToggle

    data object ConfirmSingleHabitToggle : SingleToggle

    data object CancelSingleHabitToggle : SingleToggle
  }

  /**
   * Day/week selection.
   */
  sealed interface Selection : HabitsAction {
    data class SelectDay(
      val day: ContributionDay,
      val selectionId: String,
    ) : Selection

    data class SelectWeek(
      val week: ActivityWeek,
    ) : Selection

    data object ClearSelection : Selection
  }

  /**
   * API response handling.
   */
  sealed interface Response : HabitsAction {
    data class MemoCreated(
      val memo: Memo,
    ) : Response

    data class MemoUpdated(
      val memo: Memo,
    ) : Response

    data class MemoDeleted(
      val name: String,
    ) : Response

    data class MemoOperationFailed(
      val error: String,
    ) : Response
  }

  /**
   * Cache management.
   */
  sealed interface Cache : HabitsAction {
    data class RequestPrewarmAllRanges(
      val memos: List<Memo>,
      val appMode: AppMode,
    ) : Cache

    data class UpdateActivityData(
      val range: ActivityRange,
      val mode: ActivityMode,
      val appMode: AppMode,
      val weekData: ActivitySummary,
      val configTimeline: List<HabitsConfigEntry>,
      val successRate: SuccessRate?,
    ) : Cache

    data object PrewarmCompleted : Cache

    data object InvalidateAllCache : Cache

    data class InvalidateCache(
      val range: ActivityRange,
      val mode: ActivityMode,
      val appMode: AppMode,
      val memos: List<Memo>,
    ) : Cache
  }
}
