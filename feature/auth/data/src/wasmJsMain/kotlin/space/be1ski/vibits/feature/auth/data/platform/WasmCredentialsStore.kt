package space.be1ski.vibits.feature.auth.data.platform

import kotlinx.browser.localStorage
import space.be1ski.vibits.feature.auth.data.LocalCredentials

private const val KEY_BASE_URL = "vibits_base_url"
private const val KEY_TOKEN = "vibits_token"

internal class WasmCredentialsStore : CredentialsStore {
  override fun load(): LocalCredentials {
    val baseUrl = localStorage.getItem(KEY_BASE_URL)?.trim() ?: ""
    val token = localStorage.getItem(KEY_TOKEN)?.trim() ?: ""
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  override fun save(credentials: LocalCredentials) {
    localStorage.setItem(KEY_BASE_URL, credentials.baseUrl)
    localStorage.setItem(KEY_TOKEN, credentials.token)
  }
}

actual fun createCredentialsStore(): CredentialsStore = WasmCredentialsStore()
