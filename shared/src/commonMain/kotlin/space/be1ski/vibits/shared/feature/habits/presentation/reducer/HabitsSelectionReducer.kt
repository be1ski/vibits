package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for day/week selection.
 */
internal val selectionReducer: Reducer<HabitsAction.Selection, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Selection.SelectDay -> {
        state {
          copy(
            selectedDate = action.day.date,
            activeSelectionId = action.selectionId,
          )
        }
      }

      is HabitsAction.Selection.SelectWeek -> {
        state { copy(selectedWeek = action.week) }
      }

      is HabitsAction.Selection.ClearSelection -> {
        state {
          copy(
            selectedDate = null,
            selectedWeek = null,
            activeSelectionId = null,
          )
        }
      }
    }
  }
