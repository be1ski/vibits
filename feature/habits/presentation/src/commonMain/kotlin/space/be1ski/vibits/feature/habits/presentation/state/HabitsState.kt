package space.be1ski.vibits.feature.habits.presentation.state

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.habits.domain.HabitTag
import space.be1ski.vibits.feature.habits.domain.IsSelected
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.feature.habits.domain.model.CachedActivityData
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.normalizeHabitTag
import space.be1ski.vibits.feature.memos.domain.model.Memo

/**
 * Editable habit entry for the config dialog.
 */
data class EditableHabit(
  val id: String,
  val tag: String,
  val label: String,
  val color: Long,
) {
  fun toHabitConfig(): HabitConfig {
    val finalTag = if (tag.isBlank()) normalizeHabitTag(label) else tag
    return HabitConfig(tag = finalTag, label = label, color = color)
  }

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
  val editingConfigMemo: Memo? = null,
  val showDeleteConfigConfirm: Boolean = false,
  // Edit existing config warning
  val showEditConfigWarning: Boolean = false,
  val pendingConfigEdit: List<HabitConfig> = emptyList(),
  val pendingConfigMemo: Memo? = null,
  // Selection state
  val selectedWeek: ActivityWeek? = null,
  val selectedDate: LocalDate? = null,
  val activeSelectionId: String? = null,
  // Loading state
  val isLoading: Boolean = false,
  // Cache state
  val activityDataCache: Map<ActivityCacheKey, CachedActivityData> = emptyMap(),
  val isRecalculating: Set<ActivityCacheKey> = emptySet(),
  val needsCacheRefresh: Boolean = false,
  val isInitialLoading: Boolean = false,
) {
  val isEditorOpen: Boolean get() = editorDay != null
  val isEditing: Boolean get() = editorExisting != null
  val showSingleToggleConfirm: Boolean get() = singleToggleDay != null
}
