package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState

/**
 * Sub-reducer for day/week selection.
 */
internal fun selectionReducer(
  action: HabitsAction.Selection,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.Selection, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.Selection.SelectDay -> {
        state {
          copy(
            selectedDate = a.day.date,
            activeSelectionId = a.selectionId,
          )
        }
      }

      is HabitsAction.Selection.SelectWeek -> {
        state { copy(selectedWeek = a.week) }
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
  }(action, state)
