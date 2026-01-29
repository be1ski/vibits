package space.be1ski.vibits.shared.feature.habits.presentation.reducer
import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.core.ui.theme.DefaultHabitColor
import space.be1ski.vibits.shared.feature.habits.domain.buildHabitsConfigContentFromList
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.EditableHabit
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState
import kotlin.random.Random

/**
 * Sub-reducer for config dialog management.
 */
@Suppress("LongMethod")
internal val configReducer: Reducer<HabitsAction.Config, HabitsState, HabitsEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is HabitsAction.Config.OpenConfigDialog -> {
        val editableHabits =
          action.currentConfig.mapIndexed { index, config ->
            EditableHabit.fromHabitConfig(config, "habit_$index")
          }
        state { copy(showConfigDialog = true, editingHabits = editableHabits, editingConfigMemo = action.existingMemo) }
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
          state.editingHabits.map { habit ->
            if (habit.id == action.id) {
              habit.copy(label = action.label)
            } else {
              habit
            }
          }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.Config.UpdateHabitColor -> {
        val updated =
          state.editingHabits.map { habit ->
            if (habit.id == action.id) {
              habit.copy(color = action.color)
            } else {
              habit
            }
          }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.Config.DeleteHabit -> {
        val updated = state.editingHabits.filter { it.id != action.id }
        state { copy(editingHabits = updated) }
      }

      is HabitsAction.Config.SaveConfigDialog -> {
        val validHabits =
          state.editingHabits
            .filter { it.label.isNotBlank() }
            .map { it.toHabitConfig() }
        val existingMemo = state.editingConfigMemo
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
  }
