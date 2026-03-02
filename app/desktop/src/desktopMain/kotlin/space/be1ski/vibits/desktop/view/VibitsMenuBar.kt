package space.be1ski.vibits.desktop.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction

@Composable
fun FrameWindowScope.VibitsMenuBar(features: AppFeatures) {
  MenuBar {
    Menu("File") {
      Item(
        "New Memo",
        shortcut = KeyShortcut(Key.N, meta = true),
        onClick = { features.memos.send(MemosAction.CreateDialog.ShowCreateDialog) },
      )
      Item(
        "Settings",
        shortcut = KeyShortcut(Key.Comma, meta = true),
        onClick = { openSettings(features) },
      )
    }
    Menu("View") {
      Item(
        "Habits",
        shortcut = KeyShortcut(Key.One, meta = true),
        onClick = { features.app.send(AppAction.Navigation.SelectScreen(Screen.HABITS)) },
      )
      Item(
        "Stats",
        shortcut = KeyShortcut(Key.Two, meta = true),
        onClick = { features.app.send(AppAction.Navigation.SelectScreen(Screen.STATS)) },
      )
      Item(
        "Feed",
        shortcut = KeyShortcut(Key.Three, meta = true),
        onClick = { features.app.send(AppAction.Navigation.SelectScreen(Screen.FEED)) },
      )
    }
  }
}

private fun openSettings(features: AppFeatures) {
  val memosState = features.memos.state.value
  val appState = features.app.state.value
  val settingsState = features.settings.state.value
  features.settings.send(
    SettingsAction.Dialog.Open(
      baseUrl = memosState.baseUrl,
      token = memosState.token,
      appMode = appState.appMode,
      language = settingsState.selectedLanguage,
      theme = settingsState.selectedTheme,
      syncDebounceSeconds = settingsState.selectedSyncDebounceSeconds,
    ),
  )
}
