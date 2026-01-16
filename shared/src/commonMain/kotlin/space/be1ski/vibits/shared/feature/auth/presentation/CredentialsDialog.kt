package space.be1ski.vibits.shared.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.core.logging.AppLogger
import space.be1ski.vibits.shared.core.logging.LogLevel
import space.be1ski.vibits.shared.label_access_token
import space.be1ski.vibits.shared.label_app_mode
import space.be1ski.vibits.shared.label_base_url
import space.be1ski.vibits.shared.hint_base_url
import space.be1ski.vibits.shared.action_cancel
import space.be1ski.vibits.shared.format_app_version
import space.be1ski.vibits.shared.format_credentials
import space.be1ski.vibits.shared.mode_demo_title
import space.be1ski.vibits.shared.domain.model.app.AppDetails
import space.be1ski.vibits.shared.format_environment
import space.be1ski.vibits.shared.format_memos_db
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.app.VibitsAppUiState
import space.be1ski.vibits.shared.action_reset_app
import space.be1ski.vibits.shared.action_save
import space.be1ski.vibits.shared.format_offline_storage
import space.be1ski.vibits.shared.mode_offline_title
import space.be1ski.vibits.shared.mode_online_title
import space.be1ski.vibits.shared.nav_settings
import space.be1ski.vibits.shared.label_storage
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase

@Suppress("LongParameterList")
@Composable
internal fun CredentialsDialog(
  memosState: MemosState,
  appState: VibitsAppUiState,
  dispatch: (MemosAction) -> Unit,
  appDetails: AppDetails,
  switchAppModeUseCase: SwitchAppModeUseCase,
  resetAppUseCase: ResetAppUseCase,
  onResetComplete: () -> Unit
) {
  if (!appState.showCredentialsDialog) {
    return
  }
  if (!appState.credentialsInitialized) {
    appState.editBaseUrl = memosState.baseUrl
    appState.editToken = memosState.token
    appState.credentialsInitialized = true
  }
  val scope = rememberCoroutineScope()
  androidx.compose.material3.AlertDialog(
    onDismissRequest = {
      appState.showCredentialsDialog = false
      appState.credentialsInitialized = false
      appState.credentialsDismissed = true
    },
    title = { Text(stringResource(Res.string.nav_settings)) },
    text = {
      CredentialsDialogContent(
        appState = appState,
        dispatch = dispatch,
        appDetails = appDetails,
        onModeChange = { mode ->
          scope.launch {
            switchAppModeUseCase(mode)
            appState.appMode = mode
            dispatch(MemosAction.LoadMemos)
          }
        },
        onReset = {
          scope.launch {
            resetAppUseCase()
            appState.showCredentialsDialog = false
            onResetComplete()
          }
        }
      )
    },
    confirmButton = { CredentialsDialogConfirmButton(appState, dispatch) },
    dismissButton = { CredentialsDialogDismissButton(appState) }
  )
}

@Composable
private fun CredentialsDialogContent(
  appState: VibitsAppUiState,
  dispatch: (MemosAction) -> Unit,
  appDetails: AppDetails,
  onModeChange: (AppMode) -> Unit,
  onReset: () -> Unit
) {
  var showLogs by remember { mutableStateOf(false) }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    AppModeSelector(
      currentMode = appState.appMode,
      onModeChange = onModeChange
    )
    if (appState.appMode == AppMode.Online) {
      TextField(
        value = appState.editBaseUrl,
        onValueChange = {
          appState.editBaseUrl = it
          dispatch(MemosAction.UpdateBaseUrl(it))
        },
        label = { Text(stringResource(Res.string.label_base_url)) },
        placeholder = { Text(stringResource(Res.string.hint_base_url)) },
        modifier = Modifier.fillMaxWidth()
      )
      TextField(
        value = appState.editToken,
        onValueChange = {
          appState.editToken = it
          dispatch(MemosAction.UpdateToken(it))
        },
        label = { Text(stringResource(Res.string.label_access_token)) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
      )
    }
    AppDetailsSection(appDetails, appState.appMode)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      TextButton(onClick = { showLogs = true }, modifier = Modifier.weight(1f)) {
        Text("View Logs")
      }
      TextButton(onClick = onReset, modifier = Modifier.weight(1f)) {
        Text(stringResource(Res.string.action_reset_app))
      }
    }
  }

  if (showLogs) {
    LogsDialog(onDismiss = { showLogs = false })
  }
}

@Composable
private fun AppModeSelector(
  currentMode: AppMode,
  onModeChange: (AppMode) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(stringResource(Res.string.label_app_mode))
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
      SegmentedButton(
        selected = currentMode == AppMode.Online,
        onClick = { onModeChange(AppMode.Online) },
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
      ) {
        Text(stringResource(Res.string.mode_online_title))
      }
      SegmentedButton(
        selected = currentMode == AppMode.Offline,
        onClick = { onModeChange(AppMode.Offline) },
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
      ) {
        Text(stringResource(Res.string.mode_offline_title))
      }
      SegmentedButton(
        selected = currentMode == AppMode.Demo,
        onClick = { onModeChange(AppMode.Demo) },
        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
      ) {
        Text(stringResource(Res.string.mode_demo_title))
      }
    }
  }
}

@Composable
private fun AppDetailsSection(appDetails: AppDetails, appMode: AppMode) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(stringResource(Res.string.format_app_version, appDetails.version))
    if (appMode != AppMode.Demo) {
      Text(stringResource(Res.string.label_storage))
      Text(stringResource(Res.string.format_environment, appDetails.environment))
      if (appMode == AppMode.Offline) {
        Text(stringResource(Res.string.format_offline_storage, appDetails.offlineStorage))
      } else {
        Text(stringResource(Res.string.format_credentials, appDetails.credentialsStore))
        Text(stringResource(Res.string.format_memos_db, appDetails.memosDatabase))
      }
    }
  }
}

@Composable
private fun CredentialsDialogConfirmButton(
  appState: VibitsAppUiState,
  dispatch: (MemosAction) -> Unit
) {
  Button(
    onClick = {
      dispatch(MemosAction.LoadMemos)
      appState.showCredentialsDialog = false
      appState.credentialsInitialized = false
      appState.credentialsDismissed = true
    }
  ) {
    Text(stringResource(Res.string.action_save))
  }
}

@Composable
private fun CredentialsDialogDismissButton(appState: VibitsAppUiState) {
  TextButton(
    onClick = {
      appState.showCredentialsDialog = false
      appState.credentialsInitialized = false
      appState.credentialsDismissed = true
    }
  ) {
    Text(stringResource(Res.string.action_cancel))
  }
}

private const val LOG_TIMESTAMP_LENGTH = 8

@Suppress("LongMethod")
@Composable
private fun LogsDialog(onDismiss: () -> Unit) {
  val logs = AppLogger.logs

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Logs (${logs.size})") },
    text = {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        items(logs) { entry ->
          val bgColor = when (entry.level) {
            LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
            LogLevel.WARN -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .background(bgColor, RoundedCornerShape(4.dp))
              .padding(6.dp)
          ) {
            val time = entry.timestamp.substringAfter('T').take(LOG_TIMESTAMP_LENGTH)
            val header = "$time ${entry.level.name.first()}/${entry.tag}"
            Text(
              text = header,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = entry.message,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              )
            )
          }
        }
        if (logs.isEmpty()) {
          item {
            Text(
              "No logs yet",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = { AppLogger.clear() }) {
        Text("Clear")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}
