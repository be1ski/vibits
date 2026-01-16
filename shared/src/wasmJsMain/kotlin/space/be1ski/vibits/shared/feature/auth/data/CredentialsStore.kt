package space.be1ski.vibits.shared.feature.auth.data

import kotlinx.browser.localStorage

private const val KEY_BASE_URL = "vibits_base_url"
private const val KEY_TOKEN = "vibits_token"

/**
 * Web implementation storing credentials in localStorage.
 */
actual class CredentialsStore {
  actual fun load(): LocalCredentials {
    val baseUrl = localStorage.getItem(KEY_BASE_URL)?.trim() ?: ""
    val token = localStorage.getItem(KEY_TOKEN)?.trim() ?: ""
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  actual fun save(credentials: LocalCredentials) {
    localStorage.setItem(KEY_BASE_URL, credentials.baseUrl.trim())
    localStorage.setItem(KEY_TOKEN, credentials.token.trim())
  }
}
