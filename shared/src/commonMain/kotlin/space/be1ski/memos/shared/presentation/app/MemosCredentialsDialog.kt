package space.be1ski.memos.shared.presentation.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import space.be1ski.memos.shared.domain.model.storage.StorageInfo
import space.be1ski.memos.shared.presentation.components.DEMO_MODE_LABEL
import space.be1ski.memos.shared.presentation.state.MemosUiState
import space.be1ski.memos.shared.presentation.viewmodel.MemosViewModel

@Composable
internal fun CredentialsDialog(
  uiState: MemosUiState,
  appState: MemosAppUiState,
  viewModel: MemosViewModel
) {
  if (!appState.showCredentialsDialog) {
    return
  }
  if (!appState.credentialsInitialized && uiState is MemosUiState.CredentialsInput) {
    appState.editBaseUrl = uiState.baseUrl
    appState.editToken = uiState.token
    appState.credentialsInitialized = true
  }
  androidx.compose.material3.AlertDialog(
    onDismissRequest = {
      appState.showCredentialsDialog = false
      appState.credentialsInitialized = false
      appState.credentialsDismissed = true
    },
    title = { Text("Settings") },
    text = { CredentialsDialogContent(appState, viewModel.storageInfo) },
    confirmButton = { CredentialsDialogConfirmButton(appState, viewModel) },
    dismissButton = { CredentialsDialogDismissButton(appState) }
  )
}

@Composable
private fun CredentialsDialogContent(
  appState: MemosAppUiState,
  storageInfo: StorageInfo
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    TextField(
      value = appState.editBaseUrl,
      onValueChange = { appState.editBaseUrl = it },
      label = { Text("Base URL") },
      placeholder = { Text("https://memos.example.com") },
      modifier = Modifier.fillMaxWidth()
    )
    TextField(
      value = appState.editToken,
      onValueChange = { appState.editToken = it },
      label = { Text("Access token") },
      visualTransformation = PasswordVisualTransformation(),
      modifier = Modifier.fillMaxWidth()
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(DEMO_MODE_LABEL)
      Switch(
        checked = appState.demoMode,
        onCheckedChange = { appState.demoMode = it }
      )
    }
    StorageInfoSection(storageInfo)
  }
}

@Composable
private fun StorageInfoSection(storageInfo: StorageInfo) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text("Storage")
    Text("Environment: ${storageInfo.environment}")
    Text("Credentials: ${storageInfo.credentialsStore}")
    Text("Memos DB: ${storageInfo.memosDatabase}")
  }
}

@Composable
private fun CredentialsDialogConfirmButton(
  appState: MemosAppUiState,
  viewModel: MemosViewModel
) {
  Button(
    onClick = {
      viewModel.updateBaseUrl(appState.editBaseUrl)
      viewModel.updateToken(appState.editToken)
      viewModel.loadMemos()
      appState.showCredentialsDialog = false
      appState.credentialsInitialized = false
      appState.credentialsDismissed = true
    }
  ) {
    Text("Save")
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
    Text("Cancel")
  }
}
