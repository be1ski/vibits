package space.be1ski.vibits.shared.feature.habits.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_cancel
import space.be1ski.vibits.shared.generated.action_create_new_config
import space.be1ski.vibits.shared.generated.action_edit_anyway
import space.be1ski.vibits.shared.generated.msg_edit_config_warning
import space.be1ski.vibits.shared.generated.title_edit_config_warning

@Composable
internal fun EditConfigWarningDialog(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  if (!habitsState.showEditConfigWarning) {
    return
  }

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.ConfigWarning.DismissEditConfigWarning) },
    title = {
      Text(
        text = stringResource(Res.string.title_edit_config_warning),
        style = MaterialTheme.typography.headlineSmall,
      )
    },
    text = {
      Text(
        text = stringResource(Res.string.msg_edit_config_warning),
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    confirmButton = {
      Button(onClick = { dispatch(HabitsAction.ConfigWarning.CreateNewConfigInstead) }) {
        Text(stringResource(Res.string.action_create_new_config))
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs)) {
        TextButton(onClick = { dispatch(HabitsAction.ConfigWarning.DismissEditConfigWarning) }) {
          Text(stringResource(Res.string.action_cancel))
        }
        TextButton(onClick = { dispatch(HabitsAction.ConfigWarning.ConfirmEditExistingConfig) }) {
          Text(stringResource(Res.string.action_edit_anyway))
        }
      }
    },
  )
}
