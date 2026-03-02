package space.be1ski.vibits.feature.settings.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import space.be1ski.vibits.core.strings.generated.action_save
import space.be1ski.vibits.core.strings.generated.nav_settings
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.core.utils.logging.LogEntry
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

@Composable
fun SettingsPage(
  modifier: Modifier = Modifier,
  state: SettingsState,
  dispatch: (SettingsAction) -> Unit,
  exportService: ExportService,
  testLogs: List<LogEntry>? = null,
) {
  Box(
    modifier = modifier.testTag(SettingsTestTags.SETTINGS_PAGE),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = Indent.l, vertical = Indent.m),
      verticalArrangement = Arrangement.spacedBy(Indent.m),
    ) {
      Text(
        text = stringResource(Res.string.nav_settings),
        style = MaterialTheme.typography.headlineMedium,
      )
      SettingsFormContent(state = state, dispatch = dispatch, exportService = exportService)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Indent.s, Alignment.End),
      ) {
        TextButton(onClick = { dispatch(SettingsAction.Dialog.Dismiss) }) {
          Text(stringResource(Res.string.action_cancel))
        }
        Button(onClick = { dispatch(SettingsAction.SaveAndLogs.Save) }) {
          Text(stringResource(Res.string.action_save))
        }
      }
    }
  }

  if (state.showLogsDialog) {
    LogsDialog(
      onDismiss = { dispatch(SettingsAction.SaveAndLogs.CloseLogs) },
      initialLogs = testLogs ?: Log.logs,
    )
  }

  if (state.showResetConfirmation) {
    ResetConfirmationDialog(
      onConfirm = { dispatch(SettingsAction.Reset.ConfirmReset) },
      onConfirmWithMemos = { dispatch(SettingsAction.Reset.ConfirmResetWithMemos) },
      onDismiss = { dispatch(SettingsAction.Reset.CancelReset) },
    )
  }
}
