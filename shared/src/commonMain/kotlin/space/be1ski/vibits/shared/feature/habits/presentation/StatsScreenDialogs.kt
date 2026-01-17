package space.be1ski.vibits.shared.feature.habits.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_cancel
import space.be1ski.vibits.shared.action_create
import space.be1ski.vibits.shared.action_delete
import space.be1ski.vibits.shared.action_update
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.presentation.components.localizedLabel
import space.be1ski.vibits.shared.msg_delete_day_confirm
import space.be1ski.vibits.shared.msg_toggle_habit_confirm
import space.be1ski.vibits.shared.title_create_day
import space.be1ski.vibits.shared.title_delete_day
import space.be1ski.vibits.shared.title_toggle_habit
import space.be1ski.vibits.shared.title_update_day

@Composable
internal fun HabitEditorDialog(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val habitsState = derived.habitsState
  val demoMode = derived.state.demoMode
  if (!habitsState.isEditorOpen) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.CloseEditor) },
    title = {
      val titleRes = if (habitsState.isEditing) Res.string.title_update_day else Res.string.title_create_day
      Text(stringResource(titleRes))
    },
    text = { HabitEditorContent(habitsState, demoMode, dispatch) },
    confirmButton = { HabitEditorConfirmButton(habitsState, dispatch) },
    dismissButton = { HabitEditorDismissButton(habitsState, dispatch) }
  )
}

@Composable
private fun HabitEditorContent(habitsState: HabitsState, demoMode: Boolean, dispatch: (HabitsAction) -> Unit) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    if (habitsState.editorConfig.isNotEmpty()) {
      habitsState.editorConfig.forEach { habit ->
        val tag = habit.tag
        val done = habitsState.editorSelections[tag] == true
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = done,
            onCheckedChange = { checked ->
              dispatch(HabitsAction.ToggleHabit(tag, checked))
            }
          )
          Text(habit.localizedLabel(demoMode), style = MaterialTheme.typography.bodySmall)
        }
      }
    } else {
      habitsState.editorSelections.forEach { (tag, done) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = done,
            onCheckedChange = { checked ->
              dispatch(HabitsAction.ToggleHabit(tag, checked))
            }
          )
          Text(tag, style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
  habitsState.editorError?.let { message ->
    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
  }
}

@Composable
private fun HabitEditorConfirmButton(habitsState: HabitsState, dispatch: (HabitsAction) -> Unit) {
  Button(onClick = { dispatch(HabitsAction.ConfirmEditor) }) {
    val actionRes = if (habitsState.isEditing) Res.string.action_update else Res.string.action_create
    Text(stringResource(actionRes))
  }
}

@Composable
private fun HabitEditorDismissButton(habitsState: HabitsState, dispatch: (HabitsAction) -> Unit) {
  Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs)) {
    if (habitsState.isEditing) {
      TextButton(onClick = { dispatch(HabitsAction.RequestDelete) }) {
        Text(stringResource(Res.string.action_delete))
      }
    }
    TextButton(onClick = { dispatch(HabitsAction.CloseEditor) }) {
      Text(stringResource(Res.string.action_cancel))
    }
  }
}

@Composable
internal fun EmptyDeleteDialog(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val habitsState = derived.habitsState
  if (!habitsState.showDeleteConfirm) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.CancelDelete) },
    title = { Text(stringResource(Res.string.title_delete_day)) },
    text = { Text(stringResource(Res.string.msg_delete_day_confirm)) },
    confirmButton = {
      Button(onClick = { dispatch(HabitsAction.ConfirmDelete) }) {
        Text(stringResource(Res.string.action_delete))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(HabitsAction.CancelDelete) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    }
  )
}

@Composable
internal fun SingleHabitToggleDialog(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit
) {
  val habitsState = derived.habitsState
  val day = habitsState.singleToggleDay
  val habitLabel = habitsState.singleToggleHabitLabel

  if (!habitsState.showSingleToggleConfirm || day == null || habitLabel == null) {
    return
  }

  val demoMode = derived.state.demoMode
  val habitConfig = habitsState.singleToggleConfig.firstOrNull {
    it.tag == habitsState.singleToggleHabitTag
  }
  val displayLabel = habitConfig?.localizedLabel(demoMode) ?: habitLabel

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.CancelSingleHabitToggle) },
    title = { Text(stringResource(Res.string.title_toggle_habit)) },
    text = {
      Text(stringResource(Res.string.msg_toggle_habit_confirm, displayLabel, day.date.toString()))
    },
    confirmButton = {
      Button(onClick = { dispatch(HabitsAction.ConfirmSingleHabitToggle) }) {
        Text(stringResource(Res.string.action_update))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(HabitsAction.CancelSingleHabitToggle) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    }
  )
}
