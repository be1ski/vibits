package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_cancel
import space.be1ski.vibits.core.strings.generated.action_create
import space.be1ski.vibits.core.strings.generated.action_delete
import space.be1ski.vibits.core.strings.generated.action_update
import space.be1ski.vibits.core.strings.generated.msg_delete_day_warning
import space.be1ski.vibits.core.strings.generated.msg_select_habit
import space.be1ski.vibits.core.strings.generated.title_create_day
import space.be1ski.vibits.core.strings.generated.title_delete_day
import space.be1ski.vibits.core.strings.generated.title_update_day
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.EditorError
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.components.EditConfigWarningDialog
import space.be1ski.vibits.feature.habits.presentation.view.components.HabitsConfigDialog
import space.be1ski.vibits.feature.habits.presentation.view.components.localizedLabel
import space.be1ski.vibits.feature.homescreen.domain.model.AppState

@Composable
internal fun HabitsDialogs(
  appState: AppState,
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  val demoMode = appState.isDemoMode
  EditConfigWarningDialog(habitsState, dispatch)
  HabitsConfigDialog(habitsState, demoMode, dispatch)
  HabitEditorDialog(appState, habitsState, dispatch)
  DeleteDayConfirmDialog(habitsState, dispatch)
}

@Composable
private fun HabitEditorDialog(
  appState: AppState,
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  if (!habitsState.isEditorOpen) {
    return
  }
  val demoMode = appState.isDemoMode
  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.Editor.CloseEditor) },
    modifier = Modifier.testTag(AppShellTestTags.HABIT_EDITOR_DIALOG),
    title = {
      val titleRes = if (habitsState.isEditing) Res.string.title_update_day else Res.string.title_create_day
      Text(stringResource(titleRes))
    },
    text = { HabitEditorContent(habitsState, demoMode, dispatch) },
    confirmButton = { HabitEditorConfirmButton(habitsState, dispatch) },
    dismissButton = { HabitEditorDismissButton(dispatch) },
  )
}

@Composable
private fun HabitEditorContent(
  habitsState: HabitsState,
  demoMode: Boolean,
  dispatch: (HabitsAction) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    if (habitsState.editorConfig.isNotEmpty()) {
      habitsState.editorConfig.forEach { habit ->
        val tag = habit.tag
        val done = habitsState.editorSelections[tag] == true
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = done,
            onCheckedChange = { checked ->
              dispatch(HabitsAction.Editor.ToggleHabit(tag, checked))
            },
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
              dispatch(HabitsAction.Editor.ToggleHabit(tag, checked))
            },
          )
          Text(tag, style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
  habitsState.editorError?.let { error ->
    val message =
      when (error) {
        is EditorError.NoHabitSelected -> stringResource(Res.string.msg_select_habit)
        is EditorError.OperationFailed -> error.message
      }
    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
  }
}

@Composable
private fun HabitEditorConfirmButton(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs)) {
    if (habitsState.isEditing) {
      TextButton(
        onClick = { dispatch(HabitsAction.Editor.RequestDelete) },
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
      ) {
        Text(stringResource(Res.string.action_delete))
      }
    }
    Button(onClick = { dispatch(HabitsAction.Editor.ConfirmEditor) }) {
      val actionRes = if (habitsState.isEditing) Res.string.action_update else Res.string.action_create
      Text(stringResource(actionRes))
    }
  }
}

@Composable
private fun HabitEditorDismissButton(dispatch: (HabitsAction) -> Unit) {
  TextButton(onClick = { dispatch(HabitsAction.Editor.CloseEditor) }) {
    Text(stringResource(Res.string.action_cancel))
  }
}

@Composable
private fun DeleteDayConfirmDialog(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  if (!habitsState.showDeleteConfirm) {
    return
  }

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.Editor.CancelDelete) },
    title = { Text(stringResource(Res.string.title_delete_day)) },
    text = { Text(stringResource(Res.string.msg_delete_day_warning)) },
    confirmButton = {
      Button(
        onClick = { dispatch(HabitsAction.Editor.ConfirmDelete) },
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
          ),
      ) {
        Text(stringResource(Res.string.action_delete))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(HabitsAction.Editor.CancelDelete) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}
