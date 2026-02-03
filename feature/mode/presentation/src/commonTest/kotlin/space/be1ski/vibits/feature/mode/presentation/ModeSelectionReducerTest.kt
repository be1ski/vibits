package space.be1ski.vibits.feature.mode.presentation

import space.be1ski.vibits.core.elm.test.test
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Command
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect.Notification
import space.be1ski.vibits.feature.mode.presentation.reducer.modeSelectionReducer
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionError
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import kotlin.test.Test
import kotlin.test.assertEquals

class ModeSelectionReducerTest {
  @Test
  fun `when ShowCredentialsDialog then shows dialog and clears error`() =
    modeSelectionReducer.test(ModeSelectionState(error = ModeSelectionError.FILL_ALL_FIELDS)) {
      send(ModeSelectionAction.ShowCredentialsDialog)

      assertState { showCredentialsDialog && error == null }
      assertNoEffects()
    }

  @Test
  fun `when DismissCredentialsDialog then hides dialog and resets state`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        showCredentialsDialog = true,
        baseUrl = "https://api.com",
        token = "secret",
        isValidating = true,
        error = ModeSelectionError.CONNECTION_FAILED,
      ),
    ) {
      send(ModeSelectionAction.DismissCredentialsDialog)

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
  fun `when UpdateBaseUrl then updates baseUrl and clears error`() =
    modeSelectionReducer.test(ModeSelectionState(error = ModeSelectionError.FILL_ALL_FIELDS)) {
      send(ModeSelectionAction.UpdateBaseUrl("https://new.api.com"))

      assertState { baseUrl == "https://new.api.com" && error == null }
      assertNoEffects()
    }

  @Test
  fun `when UpdateToken then updates token and clears error`() =
    modeSelectionReducer.test(ModeSelectionState(error = ModeSelectionError.FILL_ALL_FIELDS)) {
      send(ModeSelectionAction.UpdateToken("new-token"))

      assertState { token == "new-token" && error == null }
      assertNoEffects()
    }

  @Test
  fun `when Submit with empty credentials then shows error`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "", token = "")) {
      send(ModeSelectionAction.Submit)

      assertState { error == ModeSelectionError.FILL_ALL_FIELDS && !isValidating }
      assertNoEffects()
    }

  @Test
  fun `when Submit with blank baseUrl then shows error`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "  ", token = "token123")) {
      send(ModeSelectionAction.Submit)

      assertState { error == ModeSelectionError.FILL_ALL_FIELDS }
      assertNoEffects()
    }

  @Test
  fun `when Submit with blank token then shows error`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "https://api.com", token = "  ")) {
      send(ModeSelectionAction.Submit)

      assertState { error == ModeSelectionError.FILL_ALL_FIELDS }
      assertNoEffects()
    }

  @Test
  fun `when Submit with valid credentials then starts validation`() =
    modeSelectionReducer.test(ModeSelectionState(baseUrl = "https://api.com", token = "token123")) {
      send(ModeSelectionAction.Submit)

      assertState { isValidating && error == null }
      val effect = assertHasCommand<Command.ValidateCredentials>()
      assertEquals("https://api.com", effect.baseUrl)
      assertEquals("token123", effect.token)
    }

  @Test
  fun `when Submit then trims credentials before validation`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        baseUrl = "  https://api.com  ",
        token = "  token123  ",
      ),
    ) {
      send(ModeSelectionAction.Submit)

      val effect = assertHasCommand<Command.ValidateCredentials>()
      assertEquals("https://api.com", effect.baseUrl)
      assertEquals("token123", effect.token)
    }

  @Test
  fun `when ValidationSucceeded then closes dialog saves and notifies`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        showCredentialsDialog = true,
        baseUrl = "https://api.com",
        token = "token123",
        isValidating = true,
      ),
    ) {
      send(ModeSelectionAction.ValidationSucceeded)

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
  fun `when ValidationSucceeded then saves trimmed credentials`() =
    modeSelectionReducer.test(
      ModeSelectionState(
        showCredentialsDialog = true,
        baseUrl = "  https://api.com  ",
        token = "  token123  ",
        isValidating = true,
      ),
    ) {
      send(ModeSelectionAction.ValidationSucceeded)

      val effect = assertHasCommand<Command.SaveCredentials>()
      assertEquals("https://api.com", effect.baseUrl)
      assertEquals("token123", effect.token)
    }

  @Test
  fun `when ValidationSucceeded then saves ONLINE mode`() =
    modeSelectionReducer.test(ModeSelectionState(isValidating = true)) {
      send(ModeSelectionAction.ValidationSucceeded)

      val effect = assertHasCommand<Command.SaveMode>()
      assertEquals(AppMode.ONLINE, effect.mode)

      val notification = assertHasNotification<Notification.ModeSelected>()
      assertEquals(AppMode.ONLINE, notification.mode)
    }

  @Test
  fun `when ValidationFailed then stops validating and shows error`() =
    modeSelectionReducer.test(ModeSelectionState(isValidating = true)) {
      send(ModeSelectionAction.ValidationFailed)

      assertState { !isValidating && error == ModeSelectionError.CONNECTION_FAILED }
      assertNoEffects()
    }

  @Test
  fun `when SelectMode OFFLINE then saves and notifies`() =
    modeSelectionReducer.test(ModeSelectionState()) {
      send(ModeSelectionAction.SelectMode(AppMode.OFFLINE))

      assertCommandCount(1)
      val effect = assertHasCommand<Command.SaveMode>()
      assertEquals(AppMode.OFFLINE, effect.mode)

      val notification = assertHasNotification<Notification.ModeSelected>()
      assertEquals(AppMode.OFFLINE, notification.mode)
    }

  @Test
  fun `when SelectMode DEMO then saves and notifies`() =
    modeSelectionReducer.test(ModeSelectionState()) {
      send(ModeSelectionAction.SelectMode(AppMode.DEMO))

      assertCommandCount(1)
      val effect = assertHasCommand<Command.SaveMode>()
      assertEquals(AppMode.DEMO, effect.mode)

      val notification = assertHasNotification<Notification.ModeSelected>()
      assertEquals(AppMode.DEMO, notification.mode)
    }

  @Test
  fun `when StoredCredentialsFound then sets flag and shows quick online dialog`() =
    modeSelectionReducer.test(ModeSelectionState()) {
      send(ModeSelectionAction.StoredCredentialsFound)

      assertState { hasStoredCredentials && showQuickOnlineDialog }
      assertNoEffects()
    }

  @Test
  fun `when StoredCredentialsNotFound then clears flag`() =
    modeSelectionReducer.test(ModeSelectionState(hasStoredCredentials = true)) {
      send(ModeSelectionAction.StoredCredentialsNotFound)

      assertState { !hasStoredCredentials }
      assertNoEffects()
    }

  @Test
  fun `when DismissQuickOnlineDialog then hides dialog`() =
    modeSelectionReducer.test(ModeSelectionState(showQuickOnlineDialog = true)) {
      send(ModeSelectionAction.DismissQuickOnlineDialog)

      assertState { !showQuickOnlineDialog }
      assertNoEffects()
    }

  @Test
  fun `when UseStoredCredentials then closes dialog and starts validation`() =
    modeSelectionReducer.test(ModeSelectionState(showQuickOnlineDialog = true)) {
      send(ModeSelectionAction.UseStoredCredentials)

      assertState { !showQuickOnlineDialog && isValidating }
      assertCommands(Command.UseStoredCredentialsWithValidation)
    }

  @Test
  fun `when ValidationSucceeded with empty state then does not save credentials`() =
    modeSelectionReducer.test(ModeSelectionState(isValidating = true)) {
      send(ModeSelectionAction.ValidationSucceeded)

      assertState { !isValidating && baseUrl == "" && token == "" }
      assertCommandCount(1)
      assertHasCommand<Command.SaveMode>()
      assertHasNotification<Notification.ModeSelected>()
    }

  @Test
  fun `when SelectMode then closes quick online dialog`() =
    modeSelectionReducer.test(ModeSelectionState(showQuickOnlineDialog = true)) {
      send(ModeSelectionAction.SelectMode(AppMode.DEMO))

      assertState { !showQuickOnlineDialog }
      assertCommandCount(1)
    }
}
