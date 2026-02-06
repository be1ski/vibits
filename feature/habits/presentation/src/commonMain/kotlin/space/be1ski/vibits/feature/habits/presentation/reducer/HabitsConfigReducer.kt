package space.be1ski.vibits.feature.habits.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.feature.habits.domain.buildHabitsConfigContentFromList
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.EditableHabit
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import kotlin.random.Random

internal val configReducer: Reducer<HabitsAction.Config, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Config.OpenConfigDialog -> {
        val editableHabits =
          action.currentConfig.mapIndexed { index, config ->
            EditableHabit.fromHabitConfig(config, "habit_$index")
          }
        state { state.copy(showConfigDialog = true, editingHabits = editableHabits, editingConfigMemo = action.existingMemo) }
      }

      is HabitsAction.Config.CloseConfigDialog -> {
        state { state.copy(showConfigDialog = false, editingHabits = emptyList(), editingConfigMemo = null) }
      }

      is HabitsAction.Config.AddHabit -> {
        val newHabit = EditableHabit(id = "habit_${Random.nextLong()}", tag = "", label = "", color = DefaultHabitColor)
        state { state.copy(editingHabits = state.editingHabits + newHabit) }
      }

      is HabitsAction.Config.UpdateHabitLabel -> {
        state { state.copy(editingHabits = state.editingHabits.withUpdated(action.id) { it.copy(label = action.label) }) }
      }

      is HabitsAction.Config.UpdateHabitColor -> {
        state { state.copy(editingHabits = state.editingHabits.withUpdated(action.id) { it.copy(color = action.color) }) }
      }

      is HabitsAction.Config.DeleteHabit -> {
        state { state.copy(editingHabits = state.editingHabits.filter { it.id != action.id }) }
      }

      is HabitsAction.Config.SaveConfigDialog -> {
        val validHabits = state.editingHabits.filter { it.label.isNotBlank() }.map { it.toHabitConfig() }
        val existingMemo = state.editingConfigMemo
        if (existingMemo != null) {
          state {
            state.copy(showEditConfigWarning = true, pendingConfigEdit = validHabits, showConfigDialog = false)
          }
        } else {
          val content = buildHabitsConfigContentFromList(validHabits)
          state { state.copy(isLoading = true) }
          command(HabitsEffect.CreateMemo(content))
        }
      }
    }
  }

private inline fun List<EditableHabit>.withUpdated(
  id: String,
  update: (EditableHabit) -> EditableHabit,
): List<EditableHabit> = map { if (it.id == id) update(it) else it }
