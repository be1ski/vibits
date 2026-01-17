package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsReducerTest {
  // Dialog lifecycle tests

  @Test
  fun `when Open then opens dialog with provided values`() {
    val (newState, effects) =
      settingsReducer(
        SettingsAction.Open(
          baseUrl = "https://api.com",
          token = "secret",
          appMode = AppMode.ONLINE,
          language = AppLanguage.SYSTEM,
          theme = AppTheme.SYSTEM,
        ),
        SettingsState(),
      )

    assertTrue(newState.isOpen)
    assertEquals("https://api.com", newState.editBaseUrl)
    assertEquals("secret", newState.editToken)
    assertEquals(AppMode.ONLINE, newState.appMode)
    assertFalse(newState.isValidating)
    assertNull(newState.validationError)
    assertFalse(newState.showResetConfirmation)
    assertFalse(newState.showLogsDialog)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when Close then closes dialog and emits NotifyDialogClosed`() {
    val state = SettingsState(isOpen = true, showLogsDialog = true, validationError = "error")

    val (newState, effects) = settingsReducer(SettingsAction.Close, state)

    assertFalse(newState.isOpen)
    assertFalse(newState.showLogsDialog)
    assertNull(newState.validationError)
    assertEquals(1, effects.size)
    assertIs<SettingsEffect.NotifyDialogClosed>(effects.first())
  }

  @Test
  fun `when Dismiss then closes dialog and emits NotifyDialogClosed`() {
    val state = SettingsState(isOpen = true)

    val (newState, effects) = settingsReducer(SettingsAction.Dismiss, state)

    assertFalse(newState.isOpen)
    assertEquals(1, effects.size)
    assertIs<SettingsEffect.NotifyDialogClosed>(effects.first())
  }

  // Credentials tests

  @Test
  fun `when UpdateBaseUrl then updates baseUrl and emits SaveCredentials`() {
    val state = SettingsState(editToken = "token123", validationError = "old error")

    val (newState, effects) =
      settingsReducer(
        SettingsAction.UpdateBaseUrl("https://new.api.com"),
        state,
      )

    assertEquals("https://new.api.com", newState.editBaseUrl)
    assertNull(newState.validationError)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<SettingsEffect.SaveCredentials>(effect)
    assertEquals("https://new.api.com", effect.baseUrl)
    assertEquals("token123", effect.token)
  }

  @Test
  fun `when UpdateToken then updates token and emits SaveCredentials`() {
    val state = SettingsState(editBaseUrl = "https://api.com", validationError = "old error")

    val (newState, effects) =
      settingsReducer(
        SettingsAction.UpdateToken("new-token"),
        state,
      )

    assertEquals("new-token", newState.editToken)
    assertNull(newState.validationError)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<SettingsEffect.SaveCredentials>(effect)
    assertEquals("https://api.com", effect.baseUrl)
    assertEquals("new-token", effect.token)
  }

  // Mode selection tests

  @Test
  fun `when SelectMode Online with empty credentials then just shows fields without error`() {
    val state = SettingsState(editBaseUrl = "", editToken = "", appMode = AppMode.DEMO)

    val (newState, effects) =
      settingsReducer(
        SettingsAction.SelectMode(AppMode.ONLINE),
        state,
      )

    assertEquals(AppMode.ONLINE, newState.appMode)
    assertNull(newState.validationError)
    assertFalse(newState.isValidating)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when SelectMode Online with credentials then starts validation`() {
    val state = SettingsState(editBaseUrl = "https://api.com", editToken = "token123")

    val (newState, effects) =
      settingsReducer(
        SettingsAction.SelectMode(AppMode.ONLINE),
        state,
      )

    assertTrue(newState.isValidating)
    assertNull(newState.validationError)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<SettingsEffect.ValidateCredentials>(effect)
    assertEquals("https://api.com", effect.baseUrl)
    assertEquals("token123", effect.token)
    assertEquals(AppMode.ONLINE, effect.targetMode)
  }

  @Test
  fun `when SelectMode Offline then updates mode and emits SwitchMode`() {
    val state = SettingsState(appMode = AppMode.ONLINE, validationError = "old error")

    val (newState, effects) =
      settingsReducer(
        SettingsAction.SelectMode(AppMode.OFFLINE),
        state,
      )

    assertEquals(AppMode.OFFLINE, newState.appMode)
    assertNull(newState.validationError)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<SettingsEffect.SwitchMode>(effect)
    assertEquals(AppMode.OFFLINE, effect.mode)
  }

  @Test
  fun `when SelectMode Demo then updates mode and emits SwitchMode`() {
    val (newState, effects) =
      settingsReducer(
        SettingsAction.SelectMode(AppMode.DEMO),
        SettingsState(),
      )

    assertEquals(AppMode.DEMO, newState.appMode)
    assertEquals(1, effects.size)
    assertIs<SettingsEffect.SwitchMode>(effects.first())
  }

  // Validation response tests

  @Test
  fun `when ValidationSucceeded without pendingSave then just switches mode`() {
    val state = SettingsState(isOpen = true, isValidating = true, pendingSave = false)

    val (newState, effects) = settingsReducer(SettingsAction.ValidationSucceeded, state)

    assertFalse(newState.isValidating)
    assertEquals(AppMode.ONLINE, newState.appMode)
    assertTrue(newState.isOpen) // Dialog stays open
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<SettingsEffect.SwitchMode>(effect)
    assertEquals(AppMode.ONLINE, effect.mode)
  }

  @Test
  fun `when ValidationSucceeded with pendingSave then closes dialog and notifies with credentials`() {
    val state =
      SettingsState(
        isOpen = true,
        isValidating = true,
        pendingSave = true,
        editBaseUrl = "https://api.com",
        editToken = "token123",
      )

    val (newState, effects) = settingsReducer(SettingsAction.ValidationSucceeded, state)

    assertFalse(newState.isValidating)
    assertFalse(newState.isOpen)
    assertFalse(newState.pendingSave)
    assertEquals(AppMode.ONLINE, newState.appMode)
    assertEquals(2, effects.size)
    assertIs<SettingsEffect.SwitchMode>(effects[0])
    val savedEffect = effects[1]
    assertIs<SettingsEffect.NotifyCredentialsSaved>(savedEffect)
    assertEquals("https://api.com", savedEffect.baseUrl)
    assertEquals("token123", savedEffect.token)
  }

  @Test
  fun `when ValidationFailed then stops validating and shows error`() {
    val state = SettingsState(isValidating = true, pendingSave = true)

    val (newState, effects) =
      settingsReducer(
        SettingsAction.ValidationFailed("connection_failed"),
        state,
      )

    assertFalse(newState.isValidating)
    assertFalse(newState.pendingSave)
    assertEquals("connection_failed", newState.validationError)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when ModeSwitched then emits NotifyModeChanged with current mode`() {
    val state = SettingsState(appMode = AppMode.OFFLINE)

    val (_, effects) = settingsReducer(SettingsAction.ModeSwitched, state)

    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<SettingsEffect.NotifyModeChanged>(effect)
    assertEquals(AppMode.OFFLINE, effect.newMode)
  }

  // Reset flow tests

  @Test
  fun `when RequestReset then shows reset confirmation`() {
    val (newState, effects) = settingsReducer(SettingsAction.RequestReset, SettingsState())

    assertTrue(newState.showResetConfirmation)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when ConfirmReset then hides confirmation and emits ResetApp`() {
    val state = SettingsState(showResetConfirmation = true)

    val (newState, effects) = settingsReducer(SettingsAction.ConfirmReset, state)

    assertFalse(newState.showResetConfirmation)
    assertTrue(newState.isResetting)
    assertEquals(1, effects.size)
    assertIs<SettingsEffect.ResetApp>(effects.first())
  }

  @Test
  fun `when CancelReset then hides reset confirmation`() {
    val state = SettingsState(showResetConfirmation = true)

    val (newState, effects) = settingsReducer(SettingsAction.CancelReset, state)

    assertFalse(newState.showResetConfirmation)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when ResetCompleted then closes dialog and emits NotifyResetCompleted`() {
    val state = SettingsState(isOpen = true, isResetting = true)

    val (newState, effects) = settingsReducer(SettingsAction.ResetCompleted, state)

    assertFalse(newState.isOpen)
    assertFalse(newState.isResetting)
    assertEquals(1, effects.size)
    assertIs<SettingsEffect.NotifyResetCompleted>(effects.first())
  }

  // Logs dialog tests

  @Test
  fun `when OpenLogs then shows logs dialog`() {
    val (newState, effects) = settingsReducer(SettingsAction.OpenLogs, SettingsState())

    assertTrue(newState.showLogsDialog)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when CloseLogs then hides logs dialog`() {
    val state = SettingsState(showLogsDialog = true)

    val (newState, effects) = settingsReducer(SettingsAction.CloseLogs, state)

    assertFalse(newState.showLogsDialog)
    assertTrue(effects.isEmpty())
  }

  // Save tests

  @Test
  fun `when Save in Offline mode then closes dialog and notifies`() {
    val state = SettingsState(isOpen = true, appMode = AppMode.OFFLINE)

    val (newState, effects) = settingsReducer(SettingsAction.Save, state)

    assertFalse(newState.isOpen)
    assertEquals(1, effects.size)
    assertIs<SettingsEffect.NotifyCredentialsSaved>(effects.first())
  }

  @Test
  fun `when Save in Online mode with empty credentials then shows error`() {
    val state =
      SettingsState(
        isOpen = true,
        appMode = AppMode.ONLINE,
        editBaseUrl = "",
        editToken = "",
      )

    val (newState, effects) = settingsReducer(SettingsAction.Save, state)

    assertTrue(newState.isOpen)
    assertEquals("fill_all_fields", newState.validationError)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when Save in Online mode with credentials then starts validation with pendingSave`() {
    val state =
      SettingsState(
        isOpen = true,
        appMode = AppMode.ONLINE,
        editBaseUrl = "https://api.com",
        editToken = "token123",
      )

    val (newState, effects) = settingsReducer(SettingsAction.Save, state)

    assertTrue(newState.isOpen)
    assertTrue(newState.isValidating)
    assertTrue(newState.pendingSave)
    assertNull(newState.validationError)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<SettingsEffect.ValidateCredentials>(effect)
    assertEquals("https://api.com", effect.baseUrl)
    assertEquals("token123", effect.token)
  }
}
