package space.be1ski.memos.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.memos.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.memos.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.memos.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.memos.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.feature.habits.domain.HabitTag
import space.be1ski.memos.shared.feature.habits.domain.IsSelected

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

  // Config dialog
  data class OpenConfigDialog(val currentConfig: List<HabitConfig>) : HabitsAction
  data object CloseConfigDialog : HabitsAction
  data object AddHabit : HabitsAction
  data class UpdateHabitLabel(val id: String, val label: String) : HabitsAction
  data class UpdateHabitColor(val id: String, val color: Long) : HabitsAction
  data class DeleteHabit(val id: String) : HabitsAction
  data object SaveConfigDialog : HabitsAction

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
 * Editable habit entry for the config dialog.
 */
data class EditableHabit(
  val id: String,
  val tag: String,
  val label: String,
  val color: Long
) {
  fun toHabitConfig(): HabitConfig = HabitConfig(tag = tag, label = label, color = color)

  companion object {
    fun fromHabitConfig(config: HabitConfig, id: String): EditableHabit = EditableHabit(
      id = id,
      tag = config.tag,
      label = config.label,
      color = config.color
    )
  }
}

/**
 * Immutable state for the Habits feature.
 */
data class HabitsState(
  // Editor state
  val editorDay: ContributionDay? = null,
  val editorConfig: List<HabitConfig> = emptyList(),
  val editorSelections: Map<HabitTag, IsSelected> = emptyMap(),
  val editorExisting: DailyMemoInfo? = null,
  val editorError: String? = null,

  // Delete confirmation
  val showDeleteConfirm: Boolean = false,

  // Config dialog state
  val showConfigDialog: Boolean = false,
  val editingHabits: List<EditableHabit> = emptyList(),

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
