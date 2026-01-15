package space.be1ski.vibits.shared.feature.habits.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsConfigContentFromList
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsEditorSelections
import space.be1ski.vibits.shared.feature.habits.domain.normalizeHabitTag
import space.be1ski.vibits.shared.feature.habits.domain.model.DEFAULT_HABIT_COLOR
import kotlin.random.Random

/**
 * Pure reducer for the Habits feature.
 * All state transitions are deterministic and testable.
 */
val habitsReducer: Reducer<HabitsAction, HabitsState, HabitsEffect> = reducer { action, state ->
  when (action) {
    is HabitsAction.OpenEditor -> {
      val selections = buildHabitsEditorSelections(action.day, action.config)
      state {
        copy(
          editorDay = action.day,
          editorConfig = action.config,
          editorSelections = selections,
          editorExisting = action.day.dailyMemo,
          editorError = null,
          showDeleteConfirm = false
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
          showDeleteConfirm = false
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
      val editableHabits = action.currentConfig.mapIndexed { index, config ->
        EditableHabit.fromHabitConfig(config, "habit_$index")
      }
      state { copy(showConfigDialog = true, editingHabits = editableHabits) }
    }

    is HabitsAction.CloseConfigDialog -> {
      state { copy(showConfigDialog = false, editingHabits = emptyList()) }
    }

    is HabitsAction.AddHabit -> {
      val newId = "habit_${Random.nextLong()}"
      val newHabit = EditableHabit(
        id = newId,
        tag = "",
        label = "",
        color = DEFAULT_HABIT_COLOR
      )
      state { copy(editingHabits = editingHabits + newHabit) }
    }

    is HabitsAction.UpdateHabitLabel -> {
      val updated = state.editingHabits.map { habit ->
        if (habit.id == action.id) {
          habit.copy(label = action.label, tag = normalizeHabitTag(action.label))
        } else {
          habit
        }
      }
      state { copy(editingHabits = updated) }
    }

    is HabitsAction.UpdateHabitColor -> {
      val updated = state.editingHabits.map { habit ->
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
      val validHabits = state.editingHabits
        .filter { it.label.isNotBlank() }
        .map { it.toHabitConfig() }
      val content = buildHabitsConfigContentFromList(validHabits)
      state { copy(isLoading = true) }
      effect(HabitsEffect.CreateMemo(content))
    }

    is HabitsAction.SelectDay -> {
      state {
        copy(
          selectedDate = action.day.date,
          activeSelectionId = action.selectionId
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
          activeSelectionId = null
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
          editingHabits = emptyList()
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
          showDeleteConfirm = false
        )
      }
      effect(HabitsEffect.RefreshMemos)
    }

    is HabitsAction.MemoOperationFailed -> {
      state {
        copy(isLoading = false, editorError = action.error)
      }
    }
  }
}
