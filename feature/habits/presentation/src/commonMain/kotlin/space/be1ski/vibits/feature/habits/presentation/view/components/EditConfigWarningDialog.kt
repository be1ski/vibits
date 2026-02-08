package space.be1ski.vibits.feature.habits.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import space.be1ski.vibits.core.strings.generated.action_create_new_config
import space.be1ski.vibits.core.strings.generated.action_details
import space.be1ski.vibits.core.strings.generated.action_edit_anyway
import space.be1ski.vibits.core.strings.generated.msg_edit_config_warning
import space.be1ski.vibits.core.strings.generated.msg_edit_config_warning_detail
import space.be1ski.vibits.core.strings.generated.title_edit_config_warning
import space.be1ski.vibits.core.ui.ExpandableDetails
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.StatsTestTags

@Composable
fun EditConfigWarningDialog(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  if (!habitsState.showEditConfigWarning) return

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.ConfigWarning.DismissEditConfigWarning) },
    modifier = Modifier.testTag(StatsTestTags.EDIT_CONFIG_WARNING_DIALOG),
    title = { Text(stringResource(Res.string.title_edit_config_warning), style = MaterialTheme.typography.headlineSmall) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(Indent.s)) {
        Text(
          text = stringResource(Res.string.msg_edit_config_warning),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        ExpandableDetails(toggleLabel = stringResource(Res.string.action_details)) {
          Text(
            text = stringResource(Res.string.msg_edit_config_warning_detail),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs)) {
        TextButton(
          onClick = { dispatch(HabitsAction.ConfigWarning.ConfirmEditExistingConfig) },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
          Text(stringResource(Res.string.action_edit_anyway))
        }
        Button(onClick = { dispatch(HabitsAction.ConfigWarning.CreateNewConfigInstead) }) {
          Text(stringResource(Res.string.action_create_new_config))
        }
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(HabitsAction.ConfigWarning.DismissEditConfigWarning) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}
