package space.be1ski.vibits.feature.mode.presentation

import space.be1ski.vibits.core.elm.test.test
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.auth.domain.model.CredentialValidationError
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.reducer.modeSelectionReducer
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import kotlin.test.Test
import kotlin.test.assertEquals

class ModeSelectionReducerTest {
  @Test
  fun `when Dialog Show then shows dialog and clears error`() =
    modeSelectionReducer.test(ModeSelectionState(error = CredentialValidationError.FILL_ALL_FIELDS)) {
      send(ModeSelectionAction.Dialog.Show)

      assertState { showCredentialsDialog && error == null }
      assertNoEffects()
    }

  @Test
  fun `when Dialog Dismiss then hides dialog and resets state`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        showCredentialsDialog = true,
        baseUrl = "https://api.com",
        token = "secret",
        isValidating = true,
        error = CredentialValidationError.CONNECTION_FAILED,
      ),
    ) {
      send(ModeSelectionAction.Dialog.Dismiss)

      assertState {
        !showCredentialsDialog &&
          baseUrl == "" &&
          token == "" &&
          !isValidating &&
          error == null
      }
      assertNoEffects()
    }

  @Test
  fun `when Input UpdateBaseUrl then updates baseUrl and clears error`() =
    modeSelectionReducer.test(ModeSelectionState(error = CredentialValidationError.FILL_ALL_FIELDS)) {
      send(ModeSelectionAction.Input.UpdateBaseUrl("https://new.api.com"))

      assertState { baseUrl == "https://new.api.com" && error == null }
      assertNoEffects()
    }

  @Test
  fun `when Input UpdateToken then updates token and clears error`() =
    modeSelectionReducer.test(ModeSelectionState(error = CredentialValidationError.FILL_ALL_FIELDS)) {
      send(ModeSelectionAction.Input.UpdateToken("new-token"))

      assertState { token == "new-token" && error == null }
      assertNoEffects()
    }

  @Test
  fun `when Validation Submit with empty credentials then shows error`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "", token = "")) {
      send(ModeSelectionAction.Validation.Submit)

      assertState { error == CredentialValidationError.FILL_ALL_FIELDS && !isValidating }
      assertNoEffects()
    }

  @Test
  fun `when Validation Submit with blank baseUrl then shows error`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "  ", token = "token123")) {
      send(ModeSelectionAction.Validation.Submit)

      assertState { error == CredentialValidationError.FILL_ALL_FIELDS }
      assertNoEffects()
    }

  @Test
  fun `when Validation Submit with blank token then shows error`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "https://api.com", token = "  ")) {
      send(ModeSelectionAction.Validation.Submit)

      assertState { error == CredentialValidationError.FILL_ALL_FIELDS }
      assertNoEffects()
    }

  @Test
  fun `when Validation Submit with valid credentials then starts validation`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "https://api.com", token = "token123")) {
      send(ModeSelectionAction.Validation.Submit)

      assertState { isValidating && error == null }
      val effect = assertHasCommand<Command.ValidateCredentials>()
      assertEquals("https://api.com", effect.baseUrl)
      assertEquals("token123", effect.token)
    }

  @Test
  fun `when Validation Submit then trims credentials before validation`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        baseUrl = "  https://api.com  ",
        token = "  token123  ",
      ),
    ) {
      send(ModeSelectionAction.Validation.Submit)

      val effect = assertHasCommand<Command.ValidateCredentials>()
      assertEquals("https://api.com", effect.baseUrl)
      assertEquals("token123", effect.token)
    }

  @Test
  fun `when Validation Succeeded then closes dialog saves and notifies`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        showCredentialsDialog = true,
        baseUrl = "https://api.com",
        token = "token123",
        isValidating = true,
      ),
    ) {
      send(ModeSelectionAction.Validation.Succeeded)

      assertState {
        !showCredentialsDialog &&
          !isValidating &&
          baseUrl == "" &&
          token == "" &&
          error == null
      }
      assertCommandCount(2)
      assertHasCommand<Command.SaveCredentials>()
      assertHasCommand<Command.SaveMode>()
      assertHasNotification<Notification.ModeSelected>()
    }

  @Test
  fun `when Validation Succeeded then saves trimmed credentials`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        showCredentialsDialog = true,
        baseUrl = "  https://api.com  ",
        token = "  token123  ",
        isValidating = true,
      ),
    ) {
      send(ModeSelectionAction.Validation.Succeeded)

      val effect = assertHasCommand<Command.SaveCredentials>()
      assertEquals("https://api.com", effect.baseUrl)
      assertEquals("token123", effect.token)
    }

  @Test
  fun `when Validation Succeeded then saves ONLINE mode`() =
    modeSelectionReducer.test(ModeSelectionState(isValidating = true)) {
      send(ModeSelectionAction.Validation.Succeeded)

      val effect = assertHasCommand<Command.SaveMode>()
      assertEquals(AppMode.ONLINE, effect.mode)

      val notification = assertHasNotification<Notification.ModeSelected>()
      assertEquals(AppMode.ONLINE, notification.mode)
    }

  @Test
  fun `when Validation Failed then stops validating and shows error`() =
    modeSelectionReducer.test(ModeSelectionState(isValidating = true)) {
      send(ModeSelectionAction.Validation.Failed)

      assertState { !isValidating && error == CredentialValidationError.CONNECTION_FAILED }
      assertNoEffects()
    }

  @Test
  fun `when Selection SelectMode OFFLINE then saves and notifies`() =
    modeSelectionReducer.test(ModeSelectionState()) {
      send(ModeSelectionAction.Selection.SelectMode(AppMode.OFFLINE))

      assertCommandCount(1)
      val effect = assertHasCommand<Command.SaveMode>()
      assertEquals(AppMode.OFFLINE, effect.mode)

      val notification = assertHasNotification<Notification.ModeSelected>()
      assertEquals(AppMode.OFFLINE, notification.mode)
    }

  @Test
  fun `when Selection SelectMode DEMO then saves and notifies`() =
    modeSelectionReducer.test(ModeSelectionState()) {
      send(ModeSelectionAction.Selection.SelectMode(AppMode.DEMO))

      assertCommandCount(1)
      val effect = assertHasCommand<Command.SaveMode>()
      assertEquals(AppMode.DEMO, effect.mode)

      val notification = assertHasNotification<Notification.ModeSelected>()
      assertEquals(AppMode.DEMO, notification.mode)
    }

  @Test
  fun `when StoredCredentials Found then sets flag and shows quick online dialog`() =
    modeSelectionReducer.test(ModeSelectionState()) {
      send(ModeSelectionAction.StoredCredentials.Found)

      assertState { hasStoredCredentials && showQuickOnlineDialog }
      assertNoEffects()
    }

  @Test
  fun `when StoredCredentials NotFound then clears flag`() =
    modeSelectionReducer.test(ModeSelectionState(hasStoredCredentials = true)) {
      send(ModeSelectionAction.StoredCredentials.NotFound)

      assertState { !hasStoredCredentials }
      assertNoEffects()
    }

  @Test
  fun `when QuickOnline Dismiss then hides dialog`() =
    modeSelectionReducer.test(ModeSelectionState(showQuickOnlineDialog = true)) {
      send(ModeSelectionAction.QuickOnline.Dismiss)

      assertState { !showQuickOnlineDialog }
      assertNoEffects()
    }

  @Test
  fun `when QuickOnline UseStoredCredentials then closes dialog and starts validation`() =
    modeSelectionReducer.test(ModeSelectionState(showQuickOnlineDialog = true)) {
      send(ModeSelectionAction.QuickOnline.UseStoredCredentials)

      assertState { !showQuickOnlineDialog && isValidating }
      assertCommands(Command.UseStoredCredentialsWithValidation)
    }

  @Test
  fun `when Validation Succeeded with empty state then does not save credentials`() =
    modeSelectionReducer.test(ModeSelectionState(isValidating = true)) {
      send(ModeSelectionAction.Validation.Succeeded)

      assertState { !isValidating && baseUrl == "" && token == "" }
      assertCommandCount(1)
      assertHasCommand<Command.SaveMode>()
      assertHasNotification<Notification.ModeSelected>()
    }

  @Test
  fun `when Selection SelectMode then closes quick online dialog`() =
    modeSelectionReducer.test(ModeSelectionState(showQuickOnlineDialog = true)) {
      send(ModeSelectionAction.Selection.SelectMode(AppMode.DEMO))

      assertState { !showQuickOnlineDialog }
      assertCommandCount(1)
    }
}
