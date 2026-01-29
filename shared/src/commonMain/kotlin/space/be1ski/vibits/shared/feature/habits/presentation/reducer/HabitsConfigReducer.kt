package space.be1ski.vibits.shared.feature.habits.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsConfigContentFromList
import space.be1ski.vibits.shared.feature.habits.presentation.EditableHabit
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import kotlin.random.Random

/**
 * Sub-reducer for config dialog management.
 */
@Suppress("LongMethod")
internal fun configReducer(
  action: HabitsAction.Config,
  state: HabitsState,
): ReducerResult<HabitsState, HabitsEffect, Nothing> =
  reducer<HabitsAction.Config, HabitsState, HabitsEffect, Nothing> { a, s ->
    when (a) {
      is HabitsAction.Config.OpenConfigDialog -> {
        val editableHabits =
          a.currentConfig.mapIndexed { index, config ->
            EditableHabit.fromHabitConfig(config, "habit_$index")
          }
        state { copy(showConfigDialog = true, editingHabits = editableHabits, editingConfigMemo = a.existingMemo) }
      }

      is HabitsAction.Config.CloseConfigDialog -> {
        state { copy(showConfigDialog = false, editingHabits = emptyList(), editingConfigMemo = null) }
      }

      is HabitsAction.Config.AddHabit -> {
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

      is HabitsAction.Config.UpdateHabitLabel -> {
        val updated =
          s.editingHabits.map { habit ->
            if (habit.id == a.id) {
              habit.copy(label = a.label)
            } else {
              habit
            }
          }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.Config.UpdateHabitColor -> {
        val updated =
          s.editingHabits.map { habit ->
            if (habit.id == a.id) {
              habit.copy(color = a.color)
            } else {
              habit
            }
          }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.Config.DeleteHabit -> {
        val updated = s.editingHabits.filter { it.id != a.id }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.Config.SaveConfigDialog -> {
        val validHabits =
          s.editingHabits
            .filter { it.label.isNotBlank() }
            .map { it.toHabitConfig() }
        val existingMemo = s.editingConfigMemo
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
          command(HabitsEffect.CreateMemo(content))
        }
      }
    }
  }(action, state)
