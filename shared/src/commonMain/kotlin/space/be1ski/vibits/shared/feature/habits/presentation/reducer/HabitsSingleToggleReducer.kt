package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

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
          copy(
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
        state { copy(isLoading = true) }
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
