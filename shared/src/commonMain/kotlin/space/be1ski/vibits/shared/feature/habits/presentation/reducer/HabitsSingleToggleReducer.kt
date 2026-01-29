package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState

/**
 * Sub-reducer for single habit toggle from matrix.
 */
@Suppress("LongMethod")
internal fun singleToggleReducer(
  action: HabitsAction.SingleToggle,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.SingleToggle, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.SingleToggle.RequestSingleHabitToggle -> {
        state {
          copy(
            singleToggleDay = a.day,
            singleToggleHabitTag = a.habitTag,
            singleToggleHabitLabel = a.habitLabel,
            singleToggleConfig = a.config,
          )
        }
      }

      is HabitsAction.SingleToggle.ConfirmSingleHabitToggle -> {
        val day = s.singleToggleDay ?: return@reducer
        val habitTag = s.singleToggleHabitTag ?: return@reducer
        val config = s.singleToggleConfig

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
            command(HabitsEffect.DeleteMemo(existing.name))
          }
          hasAnySelection -> {
            // Build and save the memo
            val content = buildDailyContent(day.date, config, selections)
            state { copy(isLoading = true) }
            if (existing != null) {
              command(HabitsEffect.UpdateMemo(existing.name, content))
            } else {
              command(HabitsEffect.CreateMemo(content))
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
  }(action, state)
