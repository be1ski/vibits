package space.be1ski.memos.shared.config

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation backed by NSUserDefaults.
 */
actual class CredentialsStore {
  private val defaults = NSUserDefaults.standardUserDefaults

  /**
   * Loads credentials or returns empty values when not available.
   */
  actual fun load(): LocalCredentials {
    val baseUrl = defaults.stringForKey("base_url")?.trim() ?: ""
    val token = defaults.stringForKey("token")?.trim() ?: ""
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  /**
   * Saves credentials to NSUserDefaults.
   */
  actual fun save(credentials: LocalCredentials) {
    defaults.setObject(credentials.baseUrl.trim(), forKey = "base_url")
    defaults.setObject(credentials.token.trim(), forKey = "token")
  }
}
