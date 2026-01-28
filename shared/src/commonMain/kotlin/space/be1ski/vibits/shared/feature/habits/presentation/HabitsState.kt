package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.habits.domain.HabitTag
import space.be1ski.vibits.shared.feature.habits.domain.IsSelected
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData
import space.be1ski.vibits.shared.feature.habits.domain.normalizeHabitTag

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
    val finalTag = tag.ifBlank { normalizeHabitTag(label) }
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
 * Cached activity data with all related computed values.
 */
data class CachedActivityData(
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)

/**
 * Cache key for activity data.
 */
data class ActivityCacheKey(
  val range: ActivityRange,
  val mode: ActivityMode,
)

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
  val editingConfigMemo: space.be1ski.vibits.shared.feature.memos.domain.model.Memo? = null,
  val showDeleteConfigConfirm: Boolean = false,
  // Edit existing config warning
  val showEditConfigWarning: Boolean = false,
  val pendingConfigEdit: List<HabitConfig> = emptyList(),
  val pendingConfigMemo: space.be1ski.vibits.shared.feature.memos.domain.model.Memo? = null,
  // Selection state
  val selectedWeek: ActivityWeek? = null,
  val selectedDate: LocalDate? = null,
  val activeSelectionId: String? = null,
  // Loading state
  val isLoading: Boolean = false,
  // Activity data cache - NEW: Replaces Compose-level caching
  val activityDataCache: Map<ActivityCacheKey, CachedActivityData> = emptyMap(),
  val isRecalculating: Set<ActivityCacheKey> = emptySet(),
  // Track last requested range/mode to provide computed properties
  val lastRequestedRange: ActivityRange? = null,
  val lastRequestedMode: ActivityMode? = null,
) {
  val isEditorOpen: Boolean get() = editorDay != null
  val isEditing: Boolean get() = editorExisting != null
  val showSingleToggleConfirm: Boolean get() = singleToggleDay != null

  // Computed properties for requested activity data
  fun getActivityData(
    range: ActivityRange,
    mode: ActivityMode,
  ): CachedActivityData? = activityDataCache[ActivityCacheKey(range, mode)]

  fun isDataLoading(
    range: ActivityRange,
    mode: ActivityMode,
  ): Boolean = ActivityCacheKey(range, mode) in isRecalculating

  // Convenience properties for last requested data
  val currentActivityData: CachedActivityData?
    get() =
      if (lastRequestedRange != null && lastRequestedMode != null) {
        getActivityData(lastRequestedRange, lastRequestedMode)
      } else {
        null
      }

  val currentConfigTimeline: List<HabitsConfigEntry>
    get() = currentActivityData?.configTimeline ?: emptyList()

  val currentHabitsConfig: List<HabitConfig>
    get() = currentConfigTimeline.lastOrNull()?.habits ?: emptyList()

  val currentWeekData: ActivityWeekData
    get() = currentActivityData?.weekData ?: ActivityWeekData(emptyList(), 0, 0)

  val currentSuccessRate: SuccessRateData?
    get() = currentActivityData?.successRate

  val isCurrentDataLoading: Boolean
    get() =
      if (lastRequestedRange != null && lastRequestedMode != null) {
        isDataLoading(lastRequestedRange, lastRequestedMode)
      } else {
        false
      }
}
