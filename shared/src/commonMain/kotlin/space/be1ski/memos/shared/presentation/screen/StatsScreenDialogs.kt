package space.be1ski.memos.shared.presentation.screen

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
import space.be1ski.memos.shared.Res
import space.be1ski.memos.shared.action_cancel
import space.be1ski.memos.shared.action_create
import space.be1ski.memos.shared.title_create_day
import space.be1ski.memos.shared.action_delete
import space.be1ski.memos.shared.msg_delete_day_confirm
import space.be1ski.memos.shared.title_delete_day
import space.be1ski.memos.shared.core.ui.Indent
import space.be1ski.memos.shared.action_update
import space.be1ski.memos.shared.title_update_day
import space.be1ski.memos.shared.presentation.habits.HabitsAction
import space.be1ski.memos.shared.presentation.habits.HabitsState

@Composable
internal fun HabitEditorDialog(derived: StatsScreenDerivedState) {
  val habitsState = derived.habitsState
  val dispatch = derived.dispatch
  if (!habitsState.isEditorOpen) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.CloseEditor) },
    title = {
      val titleRes = if (habitsState.isEditing) Res.string.title_update_day else Res.string.title_create_day
      Text(stringResource(titleRes))
    },
    text = { HabitEditorContent(habitsState, dispatch) },
    confirmButton = { HabitEditorConfirmButton(habitsState, dispatch) },
    dismissButton = { HabitEditorDismissButton(habitsState, dispatch) }
  )
}

@Composable
private fun HabitEditorContent(habitsState: HabitsState, dispatch: (HabitsAction) -> Unit) {
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
          Text(habit.label, style = MaterialTheme.typography.bodySmall)
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
internal fun EmptyDeleteDialog(derived: StatsScreenDerivedState) {
  val habitsState = derived.habitsState
  val dispatch = derived.dispatch
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
