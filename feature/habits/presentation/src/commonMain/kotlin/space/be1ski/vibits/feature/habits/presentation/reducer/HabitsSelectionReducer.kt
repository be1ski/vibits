package space.be1ski.vibits.feature.habits.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for day/week selection.
 */
internal val selectionReducer: Reducer<HabitsAction.Selection, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Selection.SelectDay -> {
        state {
          state.copy(
            selectedDate = action.day.date,
            activeSelectionId = action.selectionId,
          )
        }
      }

      is HabitsAction.Selection.SelectWeek -> {
        state { state.copy(selectedWeek = action.week) }
      }

      is HabitsAction.Selection.ClearSelection -> {
        state {
          state.copy(
            selectedDate = null,
            selectedWeek = null,
            activeSelectionId = null,
          )
        }
      }
    }
  }
