package space.be1ski.vibits.feature.auth.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CredentialsTest {
  @Test
  fun `when both baseUrl and token are filled then isFilled returns true`() {
    val credentials = Credentials(baseUrl = "https://example.com", token = "abc123")

    assertTrue(credentials.isFilled)
  }

  @Test
  fun `when baseUrl and token have leading and trailing spaces then isFilled returns true`() {
    val credentials = Credentials(baseUrl = "  https://example.com  ", token = "  abc123  ")

    assertTrue(credentials.isFilled)
  }

  @Test
  fun `when baseUrl is blank then isFilled returns false`() {
    val credentials = Credentials(baseUrl = "", token = "abc123")

    assertFalse(credentials.isFilled)
  }

  @Test
  fun `when baseUrl is only whitespace then isFilled returns false`() {
    val credentials = Credentials(baseUrl = "   ", token = "abc123")

    assertFalse(credentials.isFilled)
  }

  @Test
  fun `when token is blank then isFilled returns false`() {
    val credentials = Credentials(baseUrl = "https://example.com", token = "")

    assertFalse(credentials.isFilled)
  }

  @Test
  fun `when token is only whitespace then isFilled returns false`() {
    val credentials = Credentials(baseUrl = "https://example.com", token = "   ")

    assertFalse(credentials.isFilled)
  }

  @Test
  fun `when both baseUrl and token are blank then isFilled returns false`() {
    val credentials = Credentials(baseUrl = "", token = "")

    assertFalse(credentials.isFilled)
  }

  @Test
  fun `when trimmed then returns credentials with trimmed values`() {
    val credentials = Credentials(baseUrl = "  https://example.com  ", token = "  abc123  ")

    val trimmed = credentials.trimmed()

    assertEquals("https://example.com", trimmed.baseUrl)
    assertEquals("abc123", trimmed.token)
  }

  @Test
  fun `when trimmed on already trimmed credentials then returns equivalent credentials`() {
    val credentials = Credentials(baseUrl = "https://example.com", token = "abc123")

    val trimmed = credentials.trimmed()

    assertEquals(credentials.baseUrl, trimmed.baseUrl)
    assertEquals(credentials.token, trimmed.token)
  }

  @Test
  fun `when requireFilled on filled credentials then returns trimmed credentials`() {
    val credentials = Credentials(baseUrl = "  https://example.com  ", token = "  abc123  ")

    val result = credentials.requireFilled()

    assertEquals("https://example.com", result.baseUrl)
    assertEquals("abc123", result.token)
  }

  @Test
  fun `when requireFilled on empty baseUrl then throws IllegalStateException`() {
    val credentials = Credentials(baseUrl = "", token = "abc123")

    val exception =
      assertFailsWith<IllegalStateException> {
        credentials.requireFilled()
      }

    assertEquals("Base URL and token are required.", exception.message)
  }

  @Test
  fun `when requireFilled on whitespace baseUrl then throws IllegalStateException`() {
    val credentials = Credentials(baseUrl = "   ", token = "abc123")

    val exception =
      assertFailsWith<IllegalStateException> {
        credentials.requireFilled()
      }

    assertEquals("Base URL and token are required.", exception.message)
  }

  @Test
  fun `when requireFilled on empty token then throws IllegalStateException`() {
    val credentials = Credentials(baseUrl = "https://example.com", token = "")

    val exception =
      assertFailsWith<IllegalStateException> {
        credentials.requireFilled()
      }

    assertEquals("Base URL and token are required.", exception.message)
  }

  @Test
  fun `when requireFilled on whitespace token then throws IllegalStateException`() {
    val credentials = Credentials(baseUrl = "https://example.com", token = "   ")

    val exception =
      assertFailsWith<IllegalStateException> {
        credentials.requireFilled()
      }

    assertEquals("Base URL and token are required.", exception.message)
  }
}
