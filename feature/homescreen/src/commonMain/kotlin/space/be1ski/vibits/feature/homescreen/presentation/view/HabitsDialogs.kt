package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.core.ui.theme.LocalWideLayout
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.EditorError
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.components.EditConfigWarningDialog
import space.be1ski.vibits.feature.habits.presentation.view.components.HabitsConfigDialog
import space.be1ski.vibits.feature.habits.presentation.view.components.localizedLabel
import space.be1ski.vibits.feature.homescreen.domain.model.AppState

private val DIALOG_TONAL_ELEVATION = 6.dp
private const val WIDE_DIALOG_WIDTH_FRACTION = 0.6f
private const val WIDE_DIALOG_HEIGHT_FRACTION = 0.8f

@Composable
internal fun HabitsDialogs(
  appState: AppState,
  habitsState: HabitsState,
  dateFormatter: DateFormatter,
  dispatch: (HabitsAction) -> Unit,
) {
  val demoMode = appState.isDemoMode
  EditConfigWarningDialog(habitsState, dispatch)
  HabitsConfigDialog(habitsState, demoMode, dispatch)
  HabitEditorDialog(appState, habitsState, dateFormatter, dispatch)
  DeleteDayConfirmDialog(habitsState, dispatch)
}

@Composable
private fun HabitEditorDialog(
  appState: AppState,
  habitsState: HabitsState,
  dateFormatter: DateFormatter,
  dispatch: (HabitsAction) -> Unit,
) {
  if (!habitsState.isEditorOpen) {
    return
  }
  val demoMode = appState.isDemoMode
  val wideLayout = LocalWideLayout.current

  Dialog(
    onDismissRequest = { dispatch(HabitsAction.Editor.CloseEditor) },
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier
          .testTag(AppShellTestTags.HABIT_EDITOR_DIALOG)
          .then(
            if (wideLayout) {
              Modifier.fillMaxWidth(WIDE_DIALOG_WIDTH_FRACTION).fillMaxHeight(WIDE_DIALOG_HEIGHT_FRACTION)
            } else {
              Modifier.fillMaxSize()
            },
          ),
      shape = if (wideLayout) MaterialTheme.shapes.extraLarge else RectangleShape,
      tonalElevation = DIALOG_TONAL_ELEVATION,
    ) {
      HabitEditorPage(habitsState, demoMode, dateFormatter, dispatch)
    }
  }
}

@Composable
private fun HabitEditorPage(
  habitsState: HabitsState,
  demoMode: Boolean,
  dateFormatter: DateFormatter,
  dispatch: (HabitsAction) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize().padding(Indent.xl)) {
    val titleRes = if (habitsState.isEditing) Res.string.title_update_day else Res.string.title_create_day
    Text(stringResource(titleRes), style = MaterialTheme.typography.headlineSmall)
    habitsState.editorDay?.let { day ->
      Text(
        dateFormatter.monthDayYear(day.date),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Spacer(Modifier.height(Indent.m))

    LazyColumn(
      modifier = Modifier.weight(1f).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(Indent.xs),
    ) {
      if (habitsState.editorConfig.isNotEmpty()) {
        items(habitsState.editorConfig, key = { it.tag }) { habit ->
          val tag = habit.tag
          val done = habitsState.editorSelections[tag] == true
          HabitEditorRow(
            label = habit.localizedLabel(demoMode),
            checked = done,
            onToggle = { dispatch(HabitsAction.Editor.ToggleHabit(tag, !done)) },
          )
        }
      } else {
        items(habitsState.editorSelections.entries.toList(), key = { it.key }) { (tag, done) ->
          HabitEditorRow(
            label = tag,
            checked = done,
            onToggle = { dispatch(HabitsAction.Editor.ToggleHabit(tag, !done)) },
          )
        }
      }
    }

    habitsState.editorError?.let { error ->
      val message =
        when (error) {
          is EditorError.NoHabitSelected -> stringResource(Res.string.msg_select_habit)
          is EditorError.OperationFailed -> error.message
        }
      Spacer(Modifier.height(Indent.xs))
      Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }

    Spacer(Modifier.height(Indent.m))
    HabitEditorActions(habitsState, dispatch)
  }
}

@Composable
private fun HabitEditorRow(
  label: String,
  checked: Boolean,
  onToggle: () -> Unit,
) {
  Surface(
    onClick = onToggle,
    modifier = Modifier.fillMaxWidth(),
    color =
      if (checked) {
        MaterialTheme.colorScheme.primaryContainer
      } else {
        MaterialTheme.colorScheme.surfaceVariant
      },
    shape = MaterialTheme.shapes.medium,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = Indent.m, vertical = Indent.s),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(Indent.m),
    ) {
      Checkbox(checked = checked, onCheckedChange = null)
      Text(label, style = MaterialTheme.typography.bodyLarge)
    }
  }
}

@Composable
private fun HabitEditorActions(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (habitsState.isEditing) {
      TextButton(
        onClick = { dispatch(HabitsAction.Editor.RequestDelete) },
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
      ) {
        Text(stringResource(Res.string.action_delete))
      }
    }
    Spacer(Modifier.weight(1f))
    TextButton(onClick = { dispatch(HabitsAction.Editor.CloseEditor) }) {
      Text(stringResource(Res.string.action_cancel))
    }
    Spacer(Modifier.width(Indent.xs))
    Button(onClick = { dispatch(HabitsAction.Editor.ConfirmEditor) }) {
      val actionRes = if (habitsState.isEditing) Res.string.action_update else Res.string.action_create
      Text(stringResource(actionRes))
    }
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
