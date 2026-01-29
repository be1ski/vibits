package space.be1ski.vibits.shared.feature.habits.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.view.components.localizedLabel
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_cancel
import space.be1ski.vibits.shared.generated.action_delete
import space.be1ski.vibits.shared.generated.action_update
import space.be1ski.vibits.shared.generated.msg_delete_day_confirm
import space.be1ski.vibits.shared.generated.msg_mark_done_confirm
import space.be1ski.vibits.shared.generated.msg_mark_not_done_confirm
import space.be1ski.vibits.shared.generated.title_delete_day
import space.be1ski.vibits.shared.generated.title_mark_done
import space.be1ski.vibits.shared.generated.title_mark_not_done

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
    },
  )
}

@Composable
internal fun SingleHabitToggleDialog(
  derived: StatsScreenDerivedState,
  dispatch: (HabitsAction) -> Unit,
) {
  val habitsState = derived.habitsState
  val day = habitsState.singleToggleDay ?: return
  val habitLabel = habitsState.singleToggleHabitLabel ?: return
  val habitTag = habitsState.singleToggleHabitTag ?: return

  if (!habitsState.showSingleToggleConfirm) {
    return
  }

  val demoMode = derived.state.demoMode
  val habitConfig = habitsState.singleToggleConfig.firstOrNull { it.tag == habitTag }
  val displayLabel = habitConfig?.localizedLabel(demoMode) ?: habitLabel
  val isCurrentlyDone = day.habitStatuses.firstOrNull { it.tag == habitTag }?.done == true

  val titleRes = if (isCurrentlyDone) Res.string.title_mark_not_done else Res.string.title_mark_done
  val messageRes = if (isCurrentlyDone) Res.string.msg_mark_not_done_confirm else Res.string.msg_mark_done_confirm

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.CancelSingleHabitToggle) },
    title = { Text(stringResource(titleRes)) },
    text = {
      Text(stringResource(messageRes, displayLabel, day.date.toString()))
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
    },
  )
}
