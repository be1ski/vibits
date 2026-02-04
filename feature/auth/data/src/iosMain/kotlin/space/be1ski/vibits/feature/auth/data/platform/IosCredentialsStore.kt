package space.be1ski.vibits.feature.auth.data.platform

import platform.Foundation.NSUserDefaults
import space.be1ski.vibits.feature.auth.data.LocalCredentials

internal class IosCredentialsStore : CredentialsStore {
  private val defaults = NSUserDefaults.standardUserDefaults

  override fun load(): LocalCredentials {
    val baseUrl = defaults.stringForKey("base_url")?.trim() ?: ""
    val token = defaults.stringForKey("token")?.trim() ?: ""
    return LocalCredentials(baseUrl = baseUrl, token = token)
  }

  override fun save(credentials: LocalCredentials) {
    defaults.setObject(credentials.baseUrl, forKey = "base_url")
    defaults.setObject(credentials.token, forKey = "token")
  }
}

actual fun createCredentialsStore(): CredentialsStore = IosCredentialsStore()
