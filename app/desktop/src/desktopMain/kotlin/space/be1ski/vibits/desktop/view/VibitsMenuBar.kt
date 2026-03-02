package space.be1ski.vibits.desktop.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.menu_file
import space.be1ski.vibits.core.strings.generated.menu_view
import space.be1ski.vibits.core.strings.generated.nav_feed
import space.be1ski.vibits.core.strings.generated.nav_habits
import space.be1ski.vibits.core.strings.generated.nav_memos
import space.be1ski.vibits.core.strings.generated.nav_settings
import space.be1ski.vibits.core.strings.generated.title_new_memo
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction

@Composable
fun FrameWindowScope.VibitsMenuBar(features: AppFeatures) {
  val menuFileLabel = stringResource(Res.string.menu_file)
  val menuViewLabel = stringResource(Res.string.menu_view)
  val newMemoLabel = stringResource(Res.string.title_new_memo)
  val settingsLabel = stringResource(Res.string.nav_settings)
  val habitsLabel = stringResource(Res.string.nav_habits)
  val memosLabel = stringResource(Res.string.nav_memos)
  val feedLabel = stringResource(Res.string.nav_feed)

  MenuBar {
    Menu(menuFileLabel) {
      Item(
        newMemoLabel,
        shortcut = KeyShortcut(Key.N, meta = true),
        onClick = { features.memos.send(MemosAction.CreateDialog.ShowCreateDialog) },
      )
      Item(
        settingsLabel,
        shortcut = KeyShortcut(Key.Comma, meta = true),
        onClick = { openSettings(features) },
      )
    }
    Menu(menuViewLabel) {
      Item(
        habitsLabel,
        shortcut = KeyShortcut(Key.One, meta = true),
        onClick = { features.app.send(AppAction.Navigation.SelectScreen(Screen.HABITS)) },
      )
      Item(
        memosLabel,
        shortcut = KeyShortcut(Key.Two, meta = true),
        onClick = { features.app.send(AppAction.Navigation.SelectScreen(Screen.STATS)) },
      )
      Item(
        feedLabel,
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
