package space.be1ski.vibits.feature.settings.presentation

import space.be1ski.vibits.core.elm.test.test
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.reducer.settingsReducer
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsReducerTest {
  @Test
  fun `when Open then opens dialog with provided values`() =
    settingsReducer.test(SettingsState()) {
      send(
        SettingsAction.Dialog.Open(
          baseUrl = "https://api.com",
          token = "secret",
          appMode = AppMode.ONLINE,
          language = AppLanguage.SYSTEM,
          theme = AppTheme.SYSTEM,
        ),
      )

      assertState {
        isOpen &&
          editBaseUrl == "https://api.com" &&
          editToken == "secret" &&
          appMode == AppMode.ONLINE &&
          !isValidating &&
          validationError == null &&
          !showResetConfirmation &&
          !showLogsDialog
      }
      assertNoEffects()
    }

  @Test
  fun `when Close then closes dialog and emits NotifyDialogClosed`() =
    settingsReducer.test(
      SettingsState(
        isOpen = true,
        showLogsDialog = true,
        validationError = "error",
      ),
    ) {
      send(SettingsAction.Dialog.Close)

      assertState { !isOpen && !showLogsDialog && validationError == null }
      assertNotifications(SettingsEffect.Notification.DialogClosed)
    }

  @Test
  fun `when Dismiss then closes dialog and emits NotifyDialogClosed`() =
    settingsReducer.test(SettingsState(isOpen = true)) {
      send(SettingsAction.Dialog.Dismiss)

      assertState { !isOpen }
      assertNotifications(SettingsEffect.Notification.DialogClosed)
    }

  @Test
  fun `when UpdateBaseUrl then updates baseUrl and clears error`() =
    settingsReducer.test(
      SettingsState(
        editToken = "token123",
        validationError = "old error",
      ),
    ) {
      send(SettingsAction.Input.UpdateBaseUrl("https://new.api.com"))

      assertState { editBaseUrl == "https://new.api.com" && validationError == null }
      assertNoEffects()
    }

  @Test
  fun `when UpdateToken then updates token and clears error`() =
    settingsReducer.test(
      SettingsState(
        editBaseUrl = "https://api.com",
        validationError = "old error",
      ),
    ) {
      send(SettingsAction.Input.UpdateToken("new-token"))

      assertState { editToken == "new-token" && validationError == null }
      assertNoEffects()
    }

  @Test
  fun `when SelectMode then updates mode without immediate effect`() =
    settingsReducer.test(
      SettingsState(
        appMode = AppMode.ONLINE,
        validationError = "old error",
      ),
    ) {
      send(SettingsAction.Input.SelectMode(AppMode.OFFLINE))

      assertState { appMode == AppMode.OFFLINE && validationError == null }
      assertNoEffects()
    }

  @Test
  fun `when SelectLanguage then updates language and marks changed`() =
    settingsReducer.test(SettingsState()) {
      send(SettingsAction.Input.SelectLanguage(AppLanguage.ENGLISH))

      assertState { selectedLanguage == AppLanguage.ENGLISH && languageChanged }
      assertNoEffects()
    }

  @Test
  fun `when SelectTheme then updates theme`() =
    settingsReducer.test(SettingsState()) {
      send(SettingsAction.Input.SelectTheme(AppTheme.DARK))

      assertState { selectedTheme == AppTheme.DARK }
      assertNoEffects()
    }

  @Test
  fun `when ValidationSucceeded then saves all settings and closes dialog`() =
    settingsReducer.test(
      SettingsState(
        isOpen = true,
        isValidating = true,
        pendingSave = true,
        editBaseUrl = "https://api.com",
        editToken = "token123",
        selectedLanguage = AppLanguage.ENGLISH,
        selectedTheme = AppTheme.DARK,
      ),
    ) {
      send(SettingsAction.Validation.ValidationSucceeded)

      assertState { !isValidating && !isOpen && !pendingSave && appMode == AppMode.ONLINE }
      assertCommandCount(4)
      assertHasCommand<SettingsEffect.Command.SaveCredentials>()
      assertHasCommand<SettingsEffect.Command.SwitchMode>()
      assertHasCommand<SettingsEffect.Command.SaveLanguage>()
      assertHasCommand<SettingsEffect.Command.SaveTheme>()
      assertHasNotification<SettingsEffect.Notification.LanguageChanged>()
      assertHasNotification<SettingsEffect.Notification.ThemeChanged>()
      assertHasNotification<SettingsEffect.Notification.CredentialsSaved>()
    }

  @Test
  fun `when ValidationFailed then stops validating and shows error`() =
    settingsReducer.test(
      SettingsState(
        isValidating = true,
        pendingSave = true,
      ),
    ) {
      send(SettingsAction.Validation.ValidationFailed("connection_failed"))

      assertState { !isValidating && !pendingSave && validationError == "connection_failed" }
      assertNoEffects()
    }

  @Test
  fun `when ModeSwitched then emits NotifyModeChanged`() =
    settingsReducer.test(SettingsState(appMode = AppMode.OFFLINE)) {
      send(SettingsAction.Input.ModeSwitched)

      val effect = assertHasNotification<SettingsEffect.Notification.ModeChanged>()
      assertEquals(AppMode.OFFLINE, effect.newMode)
    }

  @Test
  fun `when RequestReset then shows reset confirmation`() =
    settingsReducer.test(SettingsState()) {
      send(SettingsAction.Reset.RequestReset)

      assertState { showResetConfirmation }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmReset then hides confirmation and emits ResetApp`() =
    settingsReducer.test(SettingsState(showResetConfirmation = true)) {
      send(SettingsAction.Reset.ConfirmReset)

      assertState { !showResetConfirmation && isResetting }
      assertCommands(SettingsEffect.Command.ResetApp)
    }

  @Test
  fun `when ConfirmResetWithMemos then hides confirmation and emits ResetAppWithMemos`() =
    settingsReducer.test(SettingsState(showResetConfirmation = true)) {
      send(SettingsAction.Reset.ConfirmResetWithMemos)

      assertState { !showResetConfirmation && isResetting }
      assertCommands(SettingsEffect.Command.ResetAppWithMemos)
    }

  @Test
  fun `when CancelReset then hides reset confirmation`() =
    settingsReducer.test(SettingsState(showResetConfirmation = true)) {
      send(SettingsAction.Reset.CancelReset)

      assertState { !showResetConfirmation }
      assertNoEffects()
    }

  @Test
  fun `when ResetCompleted then closes dialog and emits NotifyResetCompleted`() =
    settingsReducer.test(
      SettingsState(
        isOpen = true,
        isResetting = true,
      ),
    ) {
      send(SettingsAction.Reset.ResetCompleted)

      assertState { !isOpen && !isResetting }
      assertNotifications(SettingsEffect.Notification.ResetCompleted)
    }

  @Test
  fun `when OpenLogs then shows logs dialog`() =
    settingsReducer.test(SettingsState()) {
      send(SettingsAction.SaveAndLogs.OpenLogs)

      assertState { showLogsDialog }
      assertNoEffects()
    }

  @Test
  fun `when CloseLogs then hides logs dialog`() =
    settingsReducer.test(SettingsState(showLogsDialog = true)) {
      send(SettingsAction.SaveAndLogs.CloseLogs)

      assertState { !showLogsDialog }
      assertNoEffects()
    }

  @Test
  fun `when Save in Offline mode then saves all settings and closes dialog`() =
    settingsReducer.test(
      SettingsState(
        isOpen = true,
        appMode = AppMode.OFFLINE,
        selectedLanguage = AppLanguage.RUSSIAN,
        selectedTheme = AppTheme.LIGHT,
      ),
    ) {
      send(SettingsAction.SaveAndLogs.Save)

      assertState { !isOpen }
      assertCommandCount(4)
      assertHasCommand<SettingsEffect.Command.SaveCredentials>()
      assertHasCommand<SettingsEffect.Command.SwitchMode>()
      assertHasCommand<SettingsEffect.Command.SaveLanguage>()
      assertHasCommand<SettingsEffect.Command.SaveTheme>()
    }

  @Test
  fun `when Save in Online mode with empty credentials then shows error`() =
    settingsReducer.test(
      SettingsState(
        isOpen = true,
        appMode = AppMode.ONLINE,
        editBaseUrl = "",
        editToken = "",
      ),
    ) {
      send(SettingsAction.SaveAndLogs.Save)

      assertState { isOpen && validationError == "fill_all_fields" }
      assertNoEffects()
    }

  @Test
  fun `when Save in Online mode with credentials then starts validation`() =
    settingsReducer.test(
      SettingsState(
        isOpen = true,
        appMode = AppMode.ONLINE,
        editBaseUrl = "https://api.com",
        editToken = "token123",
      ),
    ) {
      send(SettingsAction.SaveAndLogs.Save)

      assertState { isOpen && isValidating && pendingSave && validationError == null }

      val effect = assertHasCommand<SettingsEffect.Command.ValidateCredentials>()
      assertEquals("https://api.com", effect.baseUrl)
      assertEquals("token123", effect.token)
    }
}
