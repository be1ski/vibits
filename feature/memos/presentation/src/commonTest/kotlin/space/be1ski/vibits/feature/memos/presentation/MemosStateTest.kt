package space.be1ski.vibits.feature.memos.presentation

import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemosStateTest {
  @Test
  fun `when baseUrl and token are blank then hasCredentials is false`() {
    val state = MemosState(baseUrl = "", token = "")

    assertFalse(state.hasCredentials)
  }

  @Test
  fun `when baseUrl is blank and token is not then hasCredentials is false`() {
    val state = MemosState(baseUrl = "", token = "token123")

    assertFalse(state.hasCredentials)
  }

  @Test
  fun `when baseUrl is not blank and token is blank then hasCredentials is false`() {
    val state = MemosState(baseUrl = "https://api.com", token = "")

    assertFalse(state.hasCredentials)
  }

  @Test
  fun `when baseUrl and token are not blank then hasCredentials is true`() {
    val state = MemosState(baseUrl = "https://api.com", token = "token123")

    assertTrue(state.hasCredentials)
  }

  @Test
  fun `when offline mode then needsCredentials is false regardless of credentials`() {
    val stateWithoutCreds = MemosState(baseUrl = "", token = "", isOfflineMode = true)
    val stateWithCreds = MemosState(baseUrl = "https://api.com", token = "token", isOfflineMode = true)

    assertFalse(stateWithoutCreds.needsCredentials)
    assertFalse(stateWithCreds.needsCredentials)
  }

  @Test
  fun `when online mode without credentials then needsCredentials is true`() {
    val state = MemosState(baseUrl = "", token = "", isOfflineMode = false)

    assertTrue(state.needsCredentials)
  }

  @Test
  fun `when online mode with credentials then needsCredentials is false`() {
    val state = MemosState(baseUrl = "https://api.com", token = "token123", isOfflineMode = false)

    assertFalse(state.needsCredentials)
  }
}
