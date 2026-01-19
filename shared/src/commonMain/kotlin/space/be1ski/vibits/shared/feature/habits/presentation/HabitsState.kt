package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.HabitTag
import space.be1ski.vibits.shared.feature.habits.domain.IsSelected
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig

/**
 * Editable habit entry for the config dialog.
 */
data class EditableHabit(
  val id: String,
  val tag: String,
  val label: String,
  val color: Long,
) {
  fun toHabitConfig(): HabitConfig = HabitConfig(tag = tag, label = label, color = color)

  companion object {
    fun fromHabitConfig(
      config: HabitConfig,
      id: String,
    ): EditableHabit =
      EditableHabit(
        id = id,
        tag = config.tag,
        label = config.label,
        color = config.color,
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
  // Single habit toggle state
  val singleToggleDay: ContributionDay? = null,
  val singleToggleHabitTag: String? = null,
  val singleToggleHabitLabel: String? = null,
  val singleToggleConfig: List<HabitConfig> = emptyList(),
  // Config dialog state
  val showConfigDialog: Boolean = false,
  val editingHabits: List<EditableHabit> = emptyList(),
  // Selection state
  val selectedWeek: ActivityWeek? = null,
  val selectedDate: LocalDate? = null,
  val activeSelectionId: String? = null,
  // Loading state
  val isLoading: Boolean = false,
) {
  val isEditorOpen: Boolean get() = editorDay != null
  val isEditing: Boolean get() = editorExisting != null
  val showSingleToggleConfirm: Boolean get() = singleToggleDay != null
}
