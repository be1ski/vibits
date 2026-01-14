package space.be1ski.memos.shared.presentation.habits

import space.be1ski.memos.shared.domain.habits.buildDailyContent
import space.be1ski.memos.shared.domain.habits.buildHabitsConfigContent
import space.be1ski.memos.shared.domain.habits.buildHabitsEditorSelections
import space.be1ski.memos.shared.elm.Reducer
import space.be1ski.memos.shared.elm.reducer

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

    is HabitsAction.OpenConfigEditor -> {
      state { copy(showConfigEditor = true) }
    }

    is HabitsAction.CloseConfigEditor -> {
      state { copy(showConfigEditor = false, configText = "") }
    }

    is HabitsAction.UpdateConfigText -> {
      state { copy(configText = action.text) }
    }

    is HabitsAction.SaveConfig -> {
      val content = buildHabitsConfigContent(state.configText)
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
          showConfigEditor = false,
          configText = ""
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
