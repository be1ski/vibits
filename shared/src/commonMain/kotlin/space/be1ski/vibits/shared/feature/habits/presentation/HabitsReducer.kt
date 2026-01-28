package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.shared.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitStatuses
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsConfigContentFromList
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsEditorSelections
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.normalizeHabitTag
import space.be1ski.vibits.shared.feature.habits.domain.usecase.parseDailyDateFromContent
import space.be1ski.vibits.shared.feature.habits.domain.usecase.parseMemoDate
import kotlin.random.Random

/**
 * Pure reducer for the Habits feature.
 * All state transitions are deterministic and testable.
 */
val habitsReducer: Reducer<HabitsAction, HabitsState, HabitsEffect> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.OpenEditor -> {
        val day =
          when {
            action.day != null -> action.day
            action.memo != null -> {
              val timeZone = TimeZone.currentSystemDefault()
              val date =
                parseDailyDateFromContent(action.memo.content)
                  ?: parseMemoDate(action.memo, timeZone)
                  ?: return@reducer
              val habitStatuses = buildHabitStatuses(action.memo.content, action.config)
              val completedCount = habitStatuses.count { it.done }
              ContributionDay(
                date = date,
                count = completedCount,
                totalHabits = action.config.size,
                completionRatio = if (action.config.isNotEmpty()) completedCount.toFloat() / action.config.size else 0f,
                habitStatuses = habitStatuses,
                dailyMemo = DailyMemoInfo(name = action.memo.name, content = action.memo.content),
                inRange = true,
                isClickable = true,
              )
            }
            else -> return@reducer
          }
        val selections = buildHabitsEditorSelections(day, action.config)
        state {
          copy(
            editorDay = day,
            editorConfig = action.config,
            editorSelections = selections,
            editorExisting = day.dailyMemo,
            editorError = null,
            showDeleteConfirm = false,
          )
        }
      }

      is HabitsAction.CloseEditor -> {
        state {
          copy(
            editorDay = null,
            editorConfig = emptyList(),
            editorSelections = emptyMap(),
            editorExisting = null,
            editorError = null,
            showDeleteConfirm = false,
          )
        }
      }

      is HabitsAction.ToggleHabit -> {
        state {
          copy(editorSelections = editorSelections + (action.tag to action.checked))
        }
      }

      is HabitsAction.ConfirmEditor -> {
        val hasSelection = state.editorSelections.values.any { it }
        when {
          !hasSelection && state.editorExisting != null -> {
            state { copy(showDeleteConfirm = true) }
          }
          !hasSelection -> {
            state { copy(editorError = "Select at least one habit.") }
          }
          else -> {
            val day = state.editorDay ?: return@reducer
            val content = buildDailyContent(day.date, state.editorConfig, state.editorSelections)
            val existing = state.editorExisting

            state { copy(isLoading = true, editorError = null) }

            if (existing != null) {
              effect(HabitsEffect.UpdateMemo(existing.name, content))
            } else {
              effect(HabitsEffect.CreateMemo(content))
            }
          }
        }
      }

      is HabitsAction.RequestDelete -> {
        state { copy(showDeleteConfirm = true) }
      }

      is HabitsAction.ConfirmDelete -> {
        val existing = state.editorExisting ?: return@reducer
        state { copy(isLoading = true) }
        effect(HabitsEffect.DeleteMemo(existing.name))
      }

      is HabitsAction.CancelDelete -> {
        state { copy(showDeleteConfirm = false) }
      }

      is HabitsAction.OpenConfigDialog -> {
        val editableHabits =
          action.currentConfig.mapIndexed { index, config ->
            EditableHabit.fromHabitConfig(config, "habit_$index")
          }
        state { copy(showConfigDialog = true, editingHabits = editableHabits, editingConfigMemo = action.existingMemo) }
      }

      is HabitsAction.CloseConfigDialog -> {
        state { copy(showConfigDialog = false, editingHabits = emptyList(), editingConfigMemo = null) }
      }

      is HabitsAction.AddHabit -> {
        val newId = "habit_${Random.nextLong()}"
        val newHabit =
          EditableHabit(
            id = newId,
            tag = "",
            label = "",
            color = DefaultHabitColor,
          )
        state { copy(editingHabits = editingHabits + newHabit) }
      }

      is HabitsAction.UpdateHabitLabel -> {
        val updated =
          state.editingHabits.map { habit ->
            if (habit.id == action.id) {
              habit.copy(label = action.label)
            } else {
              habit
            }
          }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.UpdateHabitColor -> {
        val updated =
          state.editingHabits.map { habit ->
            if (habit.id == action.id) {
              habit.copy(color = action.color)
            } else {
              habit
            }
          }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.DeleteHabit -> {
        val updated = state.editingHabits.filter { it.id != action.id }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.SaveConfigDialog -> {
        val validHabits =
          state.editingHabits
            .filter { it.label.isNotBlank() }
            .map { it.toHabitConfig() }
        val existingMemo = state.editingConfigMemo
        if (existingMemo != null) {
          state {
            copy(
              showEditConfigWarning = true,
              pendingConfigEdit = validHabits,
              showConfigDialog = false,
            )
          }
        } else {
          val content = buildHabitsConfigContentFromList(validHabits)
          state { copy(isLoading = true) }
          effect(HabitsEffect.CreateMemo(content))
        }
      }

      is HabitsAction.DismissEditConfigWarning -> {
        state {
          copy(
            showEditConfigWarning = false,
            showConfigDialog = false,
            editingHabits = emptyList(),
            editingConfigMemo = null,
            pendingConfigEdit = emptyList(),
            pendingConfigMemo = null,
          )
        }
      }

      is HabitsAction.ConfirmEditExistingConfig -> {
        val content = buildHabitsConfigContentFromList(state.pendingConfigEdit)
        val existingMemo = state.editingConfigMemo ?: return@reducer
        state {
          copy(
            showEditConfigWarning = false,
            isLoading = true,
            pendingConfigEdit = emptyList(),
            pendingConfigMemo = null,
          )
        }
        effect(HabitsEffect.UpdateMemo(existingMemo.name, content))
      }

      is HabitsAction.CreateNewConfigInstead -> {
        val content = buildHabitsConfigContentFromList(state.pendingConfigEdit)
        state {
          copy(
            showEditConfigWarning = false,
            isLoading = true,
            pendingConfigEdit = emptyList(),
            pendingConfigMemo = null,
          )
        }
        effect(HabitsEffect.CreateMemo(content))
      }

      is HabitsAction.RequestDeleteConfig -> {
        state { copy(showDeleteConfigConfirm = true) }
      }

      is HabitsAction.ConfirmDeleteConfig -> {
        val existingMemo = state.editingConfigMemo ?: return@reducer
        state {
          copy(
            showDeleteConfigConfirm = false,
            showConfigDialog = false,
            editingHabits = emptyList(),
            editingConfigMemo = null,
            isLoading = true,
          )
        }
        effect(HabitsEffect.DeleteMemo(existingMemo.name))
      }

      is HabitsAction.CancelDeleteConfig -> {
        state { copy(showDeleteConfigConfirm = false) }
      }

      is HabitsAction.RequestSingleHabitToggle -> {
        state {
          copy(
            singleToggleDay = action.day,
            singleToggleHabitTag = action.habitTag,
            singleToggleHabitLabel = action.habitLabel,
            singleToggleConfig = action.config,
          )
        }
      }

      is HabitsAction.ConfirmSingleHabitToggle -> {
        val day = state.singleToggleDay ?: return@reducer
        val habitTag = state.singleToggleHabitTag ?: return@reducer
        val config = state.singleToggleConfig

        // Build selections by toggling the specific habit
        val currentDone = day.habitStatuses.firstOrNull { it.tag == habitTag }?.done == true
        val newDone = !currentDone

        val selections =
          config.associate { habit ->
            val wasDone = day.habitStatuses.firstOrNull { it.tag == habit.tag }?.done == true
            habit.tag to if (habit.tag == habitTag) newDone else wasDone
          }

        val hasAnySelection = selections.values.any { it }
        val existing = day.dailyMemo

        when {
          !hasAnySelection && existing != null -> {
            // All habits unchecked and memo exists - delete it
            state { copy(isLoading = true) }
            effect(HabitsEffect.DeleteMemo(existing.name))
          }
          hasAnySelection -> {
            // Build and save the memo
            val content = buildDailyContent(day.date, config, selections)
            state { copy(isLoading = true) }
            if (existing != null) {
              effect(HabitsEffect.UpdateMemo(existing.name, content))
            } else {
              effect(HabitsEffect.CreateMemo(content))
            }
          }
          else -> {
            // No selection and no existing memo - just close dialog
            state {
              copy(
                singleToggleDay = null,
                singleToggleHabitTag = null,
                singleToggleHabitLabel = null,
                singleToggleConfig = emptyList(),
              )
            }
          }
        }
      }

      is HabitsAction.CancelSingleHabitToggle -> {
        state {
          copy(
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
      }

      is HabitsAction.SelectDay -> {
        state {
          copy(
            selectedDate = action.day.date,
            activeSelectionId = action.selectionId,
          )
        }
      }

      is HabitsAction.SelectWeek -> {
        state { copy(selectedWeek = action.week) }
      }

      is HabitsAction.ClearSelection -> {
        state {
          copy(
            selectedDate = null,
            selectedWeek = null,
            activeSelectionId = null,
          )
        }
      }

      is HabitsAction.MemoCreated, is HabitsAction.MemoUpdated -> {
        state {
          copy(
            isLoading = false,
            editorDay = null,
            editorConfig = emptyList(),
            editorSelections = emptyMap(),
            editorExisting = null,
            editorError = null,
            showConfigDialog = false,
            editingHabits = emptyList(),
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
        effect(HabitsEffect.RefreshMemos)
      }

      is HabitsAction.MemoDeleted -> {
        state {
          copy(
            isLoading = false,
            editorDay = null,
            editorConfig = emptyList(),
            editorSelections = emptyMap(),
            editorExisting = null,
            editorError = null,
            showDeleteConfirm = false,
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
        effect(HabitsEffect.RefreshMemos)
      }

      is HabitsAction.MemoOperationFailed -> {
        state {
          copy(
            isLoading = false,
            editorError = action.error,
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
      }

      is HabitsAction.UpdateActivityData -> {
        val key = ActivityCacheKey(action.range, action.mode)
        val cached =
          CachedActivityData(
            weekData = action.weekData,
            configTimeline = action.configTimeline,
            successRate = action.successRate,
          )
        state {
          copy(
            activityDataCache = activityDataCache + (key to cached),
            isRecalculating = isRecalculating - key,
          )
        }
      }

      is HabitsAction.InvalidateCache -> {
        val key = ActivityCacheKey(action.range, action.mode)
        state {
          copy(
            activityDataCache = emptyMap(),
            isRecalculating = setOf(key),
            lastRequestedRange = action.range,
            lastRequestedMode = action.mode,
          )
        }
        effect(HabitsEffect.RecalculateActivityData(action.range, action.mode, action.memos))
      }
    }
  }
