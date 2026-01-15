package space.be1ski.memos.shared.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import space.be1ski.memos.shared.Res
import space.be1ski.memos.shared.label_access_token
import space.be1ski.memos.shared.label_app_mode
import space.be1ski.memos.shared.label_base_url
import space.be1ski.memos.shared.hint_base_url
import space.be1ski.memos.shared.action_cancel
import space.be1ski.memos.shared.format_app_version
import space.be1ski.memos.shared.format_credentials
import space.be1ski.memos.shared.label_demo_mode
import space.be1ski.memos.shared.domain.model.app.AppDetails
import space.be1ski.memos.shared.format_environment
import space.be1ski.memos.shared.format_memos_db
import space.be1ski.memos.shared.feature.memos.presentation.MemosAction
import space.be1ski.memos.shared.feature.memos.presentation.MemosState
import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.memos.shared.app.MemosAppUiState
import space.be1ski.memos.shared.action_reset_app
import space.be1ski.memos.shared.action_save
import space.be1ski.memos.shared.format_offline_storage
import space.be1ski.memos.shared.mode_offline_title
import space.be1ski.memos.shared.mode_online_title
import space.be1ski.memos.shared.nav_settings
import space.be1ski.memos.shared.label_storage
import space.be1ski.memos.shared.feature.mode.domain.usecase.ResetAppUseCase

@Suppress("LongParameterList")
@Composable
internal fun CredentialsDialog(
  memosState: MemosState,
  appState: MemosAppUiState,
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
  appState: MemosAppUiState,
  dispatch: (MemosAction) -> Unit,
  appDetails: AppDetails,
  onModeChange: (AppMode) -> Unit,
  onReset: () -> Unit
) {
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
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(stringResource(Res.string.label_demo_mode))
      Switch(
        checked = appState.demoMode,
        onCheckedChange = { appState.demoMode = it }
      )
    }
    AppDetailsSection(appDetails, appState.appMode)
    TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
      Text(stringResource(Res.string.action_reset_app))
    }
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
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
      ) {
        Text(stringResource(Res.string.mode_online_title))
      }
      SegmentedButton(
        selected = currentMode == AppMode.Offline,
        onClick = { onModeChange(AppMode.Offline) },
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
      ) {
        Text(stringResource(Res.string.mode_offline_title))
      }
    }
  }
}

@Composable
private fun AppDetailsSection(appDetails: AppDetails, appMode: AppMode) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(stringResource(Res.string.format_app_version, appDetails.version))
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

@Composable
private fun CredentialsDialogConfirmButton(
  appState: MemosAppUiState,
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
private fun CredentialsDialogDismissButton(appState: MemosAppUiState) {
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
