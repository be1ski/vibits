package space.be1ski.vibits.feature.habits.presentation.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_cancel
import space.be1ski.vibits.core.strings.generated.action_create_new_config
import space.be1ski.vibits.core.strings.generated.action_edit_anyway
import space.be1ski.vibits.core.strings.generated.msg_edit_config_warning
import space.be1ski.vibits.core.strings.generated.title_edit_config_warning
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.StatsTestTags

@Composable
fun EditConfigWarningDialog(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit,
) {
  if (!habitsState.showEditConfigWarning) {
    return
  }

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.ConfigWarning.DismissEditConfigWarning) },
    modifier = Modifier.testTag(StatsTestTags.EDIT_CONFIG_WARNING_DIALOG),
    title = {
      Text(
        text = stringResource(Res.string.title_edit_config_warning),
        style = MaterialTheme.typography.headlineSmall,
      )
    },
    text = {
      val fullText = stringResource(Res.string.msg_edit_config_warning)
      val paragraphs = remember(fullText) { fullText.split("\n\n") }
      Column(verticalArrangement = Arrangement.spacedBy(Indent.s)) {
        paragraphs.forEachIndexed { index, paragraph ->
          Text(
            text = paragraph,
            style =
              if (index == 0) {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              } else {
                MaterialTheme.typography.bodySmall
              },
            color =
              if (index == 0) {
                MaterialTheme.colorScheme.onSurface
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
          )
        }
      }
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
