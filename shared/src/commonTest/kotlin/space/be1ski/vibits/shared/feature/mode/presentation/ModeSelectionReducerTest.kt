package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModeSelectionReducerTest {
  // Dialog lifecycle tests

  @Test
  fun `when ShowCredentialsDialog then shows dialog and clears error`() {
    val state = ModeSelectionState(error = ModeSelectionError.FILL_ALL_FIELDS)

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.ShowCredentialsDialog, state)

    assertTrue(newState.showCredentialsDialog)
    assertNull(newState.error)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when DismissCredentialsDialog then hides dialog and resets state`() {
    val state = ModeSelectionState(
      showCredentialsDialog = true,
      baseUrl = "https://api.com",
      token = "secret",
      isValidating = true,
      error = ModeSelectionError.CONNECTION_FAILED,
    )

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.DismissCredentialsDialog, state)

    assertFalse(newState.showCredentialsDialog)
    assertEquals("", newState.baseUrl)
    assertEquals("", newState.token)
    assertFalse(newState.isValidating)
    assertNull(newState.error)
    assertTrue(effects.isEmpty())
  }

  // Credentials input tests

  @Test
  fun `when UpdateBaseUrl then updates baseUrl and clears error`() {
    val state = ModeSelectionState(error = ModeSelectionError.FILL_ALL_FIELDS)

    val (newState, effects) = modeSelectionReducer(
      ModeSelectionAction.UpdateBaseUrl("https://new.api.com"),
      state,
    )

    assertEquals("https://new.api.com", newState.baseUrl)
    assertNull(newState.error)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when UpdateToken then updates token and clears error`() {
    val state = ModeSelectionState(error = ModeSelectionError.FILL_ALL_FIELDS)

    val (newState, effects) = modeSelectionReducer(
      ModeSelectionAction.UpdateToken("new-token"),
      state,
    )

    assertEquals("new-token", newState.token)
    assertNull(newState.error)
    assertTrue(effects.isEmpty())
  }

  // Submit tests

  @Test
  fun `when Submit with empty credentials then shows error`() {
    val state = ModeSelectionState(baseUrl = "", token = "")

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.Submit, state)

    assertEquals(ModeSelectionError.FILL_ALL_FIELDS, newState.error)
    assertFalse(newState.isValidating)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when Submit with blank baseUrl then shows error`() {
    val state = ModeSelectionState(baseUrl = "  ", token = "token123")

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.Submit, state)

    assertEquals(ModeSelectionError.FILL_ALL_FIELDS, newState.error)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when Submit with blank token then shows error`() {
    val state = ModeSelectionState(baseUrl = "https://api.com", token = "  ")

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.Submit, state)

    assertEquals(ModeSelectionError.FILL_ALL_FIELDS, newState.error)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when Submit with valid credentials then starts validation`() {
    val state = ModeSelectionState(baseUrl = "https://api.com", token = "token123")

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.Submit, state)

    assertTrue(newState.isValidating)
    assertNull(newState.error)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<ModeSelectionEffect.ValidateCredentials>(effect)
    assertEquals("https://api.com", effect.baseUrl)
    assertEquals("token123", effect.token)
  }

  @Test
  fun `when Submit then trims credentials before validation`() {
    val state = ModeSelectionState(baseUrl = "  https://api.com  ", token = "  token123  ")

    val (_, effects) = modeSelectionReducer(ModeSelectionAction.Submit, state)

    val effect = effects.first()
    assertIs<ModeSelectionEffect.ValidateCredentials>(effect)
    assertEquals("https://api.com", effect.baseUrl)
    assertEquals("token123", effect.token)
  }

  // Validation response tests

  @Test
  fun `when ValidationSucceeded then closes dialog saves and notifies`() {
    val state = ModeSelectionState(
      showCredentialsDialog = true,
      baseUrl = "https://api.com",
      token = "token123",
      isValidating = true,
    )

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.ValidationSucceeded, state)

    assertFalse(newState.showCredentialsDialog)
    assertFalse(newState.isValidating)
    assertEquals("", newState.baseUrl)
    assertEquals("", newState.token)
    assertNull(newState.error)
    assertEquals(3, effects.size)
    assertIs<ModeSelectionEffect.SaveCredentials>(effects[0])
    assertIs<ModeSelectionEffect.SaveMode>(effects[1])
    assertIs<ModeSelectionEffect.NotifyModeSelected>(effects[2])
  }

  @Test
  fun `when ValidationSucceeded then saves trimmed credentials`() {
    val state = ModeSelectionState(
      showCredentialsDialog = true,
      baseUrl = "  https://api.com  ",
      token = "  token123  ",
      isValidating = true,
    )

    val (_, effects) = modeSelectionReducer(ModeSelectionAction.ValidationSucceeded, state)

    val saveEffect = effects[0]
    assertIs<ModeSelectionEffect.SaveCredentials>(saveEffect)
    assertEquals("https://api.com", saveEffect.baseUrl)
    assertEquals("token123", saveEffect.token)
  }

  @Test
  fun `when ValidationSucceeded then saves ONLINE mode`() {
    val state = ModeSelectionState(isValidating = true)

    val (_, effects) = modeSelectionReducer(ModeSelectionAction.ValidationSucceeded, state)

    val saveModeEffect = effects[1]
    assertIs<ModeSelectionEffect.SaveMode>(saveModeEffect)
    assertEquals(AppMode.ONLINE, saveModeEffect.mode)

    val notifyEffect = effects[2]
    assertIs<ModeSelectionEffect.NotifyModeSelected>(notifyEffect)
    assertEquals(AppMode.ONLINE, notifyEffect.mode)
  }

  @Test
  fun `when ValidationFailed then stops validating and shows error`() {
    val state = ModeSelectionState(isValidating = true)

    val (newState, effects) = modeSelectionReducer(ModeSelectionAction.ValidationFailed, state)

    assertFalse(newState.isValidating)
    assertEquals(ModeSelectionError.CONNECTION_FAILED, newState.error)
    assertTrue(effects.isEmpty())
  }

  // Mode selection tests

  @Test
  fun `when SelectMode OFFLINE then saves and notifies`() {
    val (newState, effects) = modeSelectionReducer(
      ModeSelectionAction.SelectMode(AppMode.OFFLINE),
      ModeSelectionState(),
    )

    assertEquals(2, effects.size)
    val saveModeEffect = effects[0]
    assertIs<ModeSelectionEffect.SaveMode>(saveModeEffect)
    assertEquals(AppMode.OFFLINE, saveModeEffect.mode)

    val notifyEffect = effects[1]
    assertIs<ModeSelectionEffect.NotifyModeSelected>(notifyEffect)
    assertEquals(AppMode.OFFLINE, notifyEffect.mode)
  }

  @Test
  fun `when SelectMode DEMO then saves and notifies`() {
    val (_, effects) = modeSelectionReducer(
      ModeSelectionAction.SelectMode(AppMode.DEMO),
      ModeSelectionState(),
    )

    assertEquals(2, effects.size)
    val saveModeEffect = effects[0]
    assertIs<ModeSelectionEffect.SaveMode>(saveModeEffect)
    assertEquals(AppMode.DEMO, saveModeEffect.mode)

    val notifyEffect = effects[1]
    assertIs<ModeSelectionEffect.NotifyModeSelected>(notifyEffect)
    assertEquals(AppMode.DEMO, notifyEffect.mode)
  }
}
