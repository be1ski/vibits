package space.be1ski.memos.shared.presentation.habits

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.components.ActivityWeek
import space.be1ski.memos.shared.presentation.components.ContributionDay
import space.be1ski.memos.shared.presentation.components.DailyMemoInfo
import space.be1ski.memos.shared.presentation.components.HabitConfig

/**
 * Actions for the Habits feature.
 */
sealed interface HabitsAction {
  // Editor lifecycle
  data class OpenEditor(
    val day: ContributionDay,
    val config: List<HabitConfig>
  ) : HabitsAction

  data object CloseEditor : HabitsAction

  // Editor interactions
  data class ToggleHabit(val tag: String, val checked: Boolean) : HabitsAction
  data object ConfirmEditor : HabitsAction
  data object RequestDelete : HabitsAction
  data object ConfirmDelete : HabitsAction
  data object CancelDelete : HabitsAction

  // Config management
  data object OpenConfigEditor : HabitsAction
  data object CloseConfigEditor : HabitsAction
  data class UpdateConfigText(val text: String) : HabitsAction
  data object SaveConfig : HabitsAction

  // Selection management
  data class SelectDay(val day: ContributionDay, val selectionId: String) : HabitsAction
  data class SelectWeek(val week: ActivityWeek) : HabitsAction
  data object ClearSelection : HabitsAction

  // API responses
  data class MemoCreated(val memo: Memo) : HabitsAction
  data class MemoUpdated(val memo: Memo) : HabitsAction
  data class MemoDeleted(val name: String) : HabitsAction
  data class MemoOperationFailed(val error: String) : HabitsAction
}

/**
 * Immutable state for the Habits feature.
 */
data class HabitsState(
  // Editor state
  val editorDay: ContributionDay? = null,
  val editorConfig: List<HabitConfig> = emptyList(),
  val editorSelections: Map<String, Boolean> = emptyMap(),
  val editorExisting: DailyMemoInfo? = null,
  val editorError: String? = null,

  // Delete confirmation
  val showDeleteConfirm: Boolean = false,

  // Config editor state
  val showConfigEditor: Boolean = false,
  val configText: String = "",

  // Selection state
  val selectedWeek: ActivityWeek? = null,
  val selectedDate: LocalDate? = null,
  val activeSelectionId: String? = null,

  // Loading state
  val isLoading: Boolean = false
) {
  val isEditorOpen: Boolean get() = editorDay != null
  val isEditing: Boolean get() = editorExisting != null
  val hasSelection: Boolean get() = editorSelections.values.any { it }
}

/**
 * Side effects for the Habits feature.
 */
sealed interface HabitsEffect {
  data class CreateMemo(val content: String) : HabitsEffect
  data class UpdateMemo(val name: String, val content: String) : HabitsEffect
  data class DeleteMemo(val name: String) : HabitsEffect
  data object RefreshMemos : HabitsEffect
}
