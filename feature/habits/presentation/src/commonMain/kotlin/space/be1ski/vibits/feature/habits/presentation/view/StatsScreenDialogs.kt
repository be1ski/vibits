package space.be1ski.vibits.feature.habits.presentation.view
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_cancel
import space.be1ski.vibits.core.strings.generated.action_delete
import space.be1ski.vibits.core.strings.generated.action_update
import space.be1ski.vibits.core.strings.generated.msg_delete_day_confirm
import space.be1ski.vibits.core.strings.generated.msg_mark_done_confirm
import space.be1ski.vibits.core.strings.generated.msg_mark_not_done_confirm
import space.be1ski.vibits.core.strings.generated.title_delete_day
import space.be1ski.vibits.core.strings.generated.title_mark_done
import space.be1ski.vibits.core.strings.generated.title_mark_not_done
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.view.components.localizedLabel

@Composable
internal fun EmptyDeleteDialog(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit,
) {
  val habitsState = derived.habitsState
  if (!habitsState.showDeleteConfirm) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.Editor.CancelDelete) },
    modifier = Modifier.testTag(StatsTestTags.EMPTY_DELETE_DIALOG),
    title = { Text(stringResource(Res.string.title_delete_day)) },
    text = { Text(stringResource(Res.string.msg_delete_day_confirm)) },
    confirmButton = {
      Button(
        onClick = { dispatch(HabitsAction.Editor.ConfirmDelete) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
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

@Composable
internal fun SingleHabitToggleDialog(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit,
) {
  val habitsState = derived.habitsState
  val day = habitsState.singleToggleDay
  val habitTag = habitsState.singleToggleHabitTag
  if (!habitsState.showSingleToggleConfirm || day == null || habitTag == null) return

  val demoMode = derived.state.demoMode
  val habitLabel = habitsState.singleToggleHabitLabel.orEmpty()
  val habitConfig = habitsState.singleToggleConfig.firstOrNull { it.tag == habitTag }
  val displayLabel = habitConfig?.localizedLabel(demoMode) ?: habitLabel
  val isCurrentlyDone = day.habitStatuses.firstOrNull { it.tag == habitTag }?.done == true

  val titleRes = if (isCurrentlyDone) Res.string.title_mark_not_done else Res.string.title_mark_done
  val messageRes = if (isCurrentlyDone) Res.string.msg_mark_not_done_confirm else Res.string.msg_mark_done_confirm

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.SingleToggle.CancelSingleHabitToggle) },
    modifier = Modifier.testTag(StatsTestTags.SINGLE_TOGGLE_DIALOG),
    title = { Text(stringResource(titleRes)) },
    text = {
      Text(stringResource(messageRes, displayLabel, day.date.toString()))
    },
    confirmButton = {
      Button(onClick = { dispatch(HabitsAction.SingleToggle.ConfirmSingleHabitToggle) }) {
        Text(stringResource(Res.string.action_update))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(HabitsAction.SingleToggle.CancelSingleHabitToggle) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}
