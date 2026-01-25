package space.be1ski.vibits.shared.feature.memos.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemosContentTest {
  @Test
  fun `when baseUrl and token are blank then hasCredentials is false`() {
    val content = MemosContent(baseUrl = "", token = "")

    assertFalse(content.hasCredentials)
  }

  @Test
  fun `when baseUrl is blank and token is not then hasCredentials is false`() {
    val content = MemosContent(baseUrl = "", token = "token123")

    assertFalse(content.hasCredentials)
  }

  @Test
  fun `when baseUrl is not blank and token is blank then hasCredentials is false`() {
    val content = MemosContent(baseUrl = "https://api.com", token = "")

    assertFalse(content.hasCredentials)
  }

  @Test
  fun `when baseUrl and token are not blank then hasCredentials is true`() {
    val content = MemosContent(baseUrl = "https://api.com", token = "token123")

    assertTrue(content.hasCredentials)
  }

  @Test
  fun `when offline mode then needsCredentials is false regardless of credentials`() {
    val contentWithoutCreds = MemosContent(baseUrl = "", token = "", isOfflineMode = true)
    val contentWithCreds = MemosContent(baseUrl = "https://api.com", token = "token", isOfflineMode = true)

    assertFalse(contentWithoutCreds.needsCredentials)
    assertFalse(contentWithCreds.needsCredentials)
  }

  @Test
  fun `when online mode without credentials then needsCredentials is true`() {
    val content = MemosContent(baseUrl = "", token = "", isOfflineMode = false)

    assertTrue(content.needsCredentials)
  }

  @Test
  fun `when online mode with credentials then needsCredentials is false`() {
    val content = MemosContent(baseUrl = "https://api.com", token = "token123", isOfflineMode = false)

    assertFalse(content.needsCredentials)
  }
}
