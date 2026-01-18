package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.core.elm.test
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
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
      val effect = assertHasEffect<ModeSelectionEffect.ValidateCredentials>()
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

      val effect = assertHasEffect<ModeSelectionEffect.ValidateCredentials>()
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
      assertEffectCount(3)
      assertHasEffect<ModeSelectionEffect.SaveCredentials>()
      assertHasEffect<ModeSelectionEffect.SaveMode>()
      assertHasEffect<ModeSelectionEffect.NotifyModeSelected>()
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

      val saveEffect = assertHasEffect<ModeSelectionEffect.SaveCredentials>()
      assertEquals("https://api.com", saveEffect.baseUrl)
      assertEquals("token123", saveEffect.token)
    }

  @Test
  fun `when ValidationSucceeded then saves ONLINE mode`() =
    modeSelectionReducer.test(ModeSelectionState(isValidating = true)) {
      send(ModeSelectionAction.ValidationSucceeded)

      val saveModeEffect = assertHasEffect<ModeSelectionEffect.SaveMode>()
      assertEquals(AppMode.ONLINE, saveModeEffect.mode)

      val notifyEffect = assertHasEffect<ModeSelectionEffect.NotifyModeSelected>()
      assertEquals(AppMode.ONLINE, notifyEffect.mode)
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

      assertEffectCount(2)
      val saveModeEffect = assertHasEffect<ModeSelectionEffect.SaveMode>()
      assertEquals(AppMode.OFFLINE, saveModeEffect.mode)

      val notifyEffect = assertHasEffect<ModeSelectionEffect.NotifyModeSelected>()
      assertEquals(AppMode.OFFLINE, notifyEffect.mode)
    }

  @Test
  fun `when SelectMode DEMO then saves and notifies`() =
    modeSelectionReducer.test(ModeSelectionState()) {
      send(ModeSelectionAction.SelectMode(AppMode.DEMO))

      assertEffectCount(2)
      val saveModeEffect = assertHasEffect<ModeSelectionEffect.SaveMode>()
      assertEquals(AppMode.DEMO, saveModeEffect.mode)

      val notifyEffect = assertHasEffect<ModeSelectionEffect.NotifyModeSelected>()
      assertEquals(AppMode.DEMO, notifyEffect.mode)
    }
}
