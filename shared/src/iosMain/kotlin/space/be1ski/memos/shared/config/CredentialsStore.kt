package space.be1ski.memos.shared.config

import platform.Foundation.NSUserDefaults

actual class CredentialsStore {
  private val defaults = NSUserDefaults.standardUserDefaults

  actual fun load(): LocalCredentials {
    val baseUrl = defaults.stringForKey("base_url")?.trim() ?: ""
    val token = defaults.stringForKey("token")?.trim() ?: ""
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  actual fun save(credentials: LocalCredentials) {
    defaults.setObject(credentials.baseUrl.trim(), forKey = "base_url")
    defaults.setObject(credentials.token.trim(), forKey = "token")
  }
}
