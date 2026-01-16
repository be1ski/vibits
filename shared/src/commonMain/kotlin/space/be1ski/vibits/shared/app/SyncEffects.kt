package space.be1ski.vibits.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

@Composable
internal fun SyncAutoLoad(
  memosState: MemosState,
  appState: VibitsAppUiState,
  dispatch: (MemosAction) -> Unit
) {
  LaunchedEffect(memosState.credentialsMode, appState.autoLoaded, memosState.isLoading, appState.appMode) {
    val skipCredentialsCheck = appState.appMode == AppMode.Demo || appState.appMode == AppMode.Offline
    val shouldAutoLoad = !memosState.credentialsMode &&
      !appState.autoLoaded &&
      !memosState.isLoading &&
      (skipCredentialsCheck || memosState.hasCredentials)
    if (shouldAutoLoad) {
      appState.autoLoaded = true
      dispatch(MemosAction.LoadMemos)
    }
  }
}

@Composable
internal fun SyncCredentialsDialog(
  memosState: MemosState,
  appState: VibitsAppUiState
) {
  LaunchedEffect(memosState.credentialsMode, appState.showCredentialsDialog, appState.credentialsDismissed) {
    if (memosState.credentialsMode && !appState.showCredentialsDialog && !appState.credentialsDismissed) {
      appState.showCredentialsDialog = true
      appState.credentialsInitialized = false
    }
    if (!memosState.credentialsMode) {
      appState.credentialsDismissed = false
    }
  }
}
