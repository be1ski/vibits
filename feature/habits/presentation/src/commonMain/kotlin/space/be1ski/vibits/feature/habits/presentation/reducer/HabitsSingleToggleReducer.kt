package space.be1ski.vibits.feature.habits.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for single habit toggle from matrix.
 * Uses ToggleDailyHabit effect which atomically reads current state,
 * applies the toggle, and saves - preventing race conditions.
 */
internal val singleToggleReducer: Reducer<HabitsAction.SingleToggle, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.SingleToggle.RequestSingleHabitToggle -> {
        state {
          state.copy(
            singleToggleDay = action.day,
            singleToggleHabitTag = action.habitTag,
            singleToggleHabitLabel = action.habitLabel,
            singleToggleConfig = action.config,
          )
        }
      }

      is HabitsAction.SingleToggle.ConfirmSingleHabitToggle -> {
        val day = state.singleToggleDay ?: return@reducer
        val habitTag = state.singleToggleHabitTag ?: return@reducer
        val config = state.singleToggleConfig

        // Use atomic toggle operation that reads current state from cache
        // This prevents race conditions when rapidly toggling multiple habits
        state { state.copy(isLoading = true) }
        command(
          HabitsEffect.ToggleDailyHabit(
            date = day.date,
            habitTag = habitTag,
            habitsConfig = config,
          ),
        )
      }

      is HabitsAction.SingleToggle.CancelSingleHabitToggle -> {
        state {
          state.copy(
            singleToggleDay = null,
            singleToggleHabitTag = null,
            singleToggleHabitLabel = null,
            singleToggleConfig = emptyList(),
          )
        }
      }
    }
  }
