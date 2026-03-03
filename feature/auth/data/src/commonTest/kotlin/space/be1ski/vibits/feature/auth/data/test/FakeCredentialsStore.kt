package space.be1ski.vibits.feature.auth.data.test

import space.be1ski.vibits.feature.auth.data.LocalCredentials
import space.be1ski.vibits.feature.auth.data.platform.CredentialsStore

class FakeCredentialsStore(
  initial: LocalCredentials = LocalCredentials(baseUrl = "", token = ""),
  private val secureStorageCredentials: LocalCredentials? = null,
  private val secureStorageAvailable: Boolean = false,
) : CredentialsStore {
  var stored: LocalCredentials = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun load(): LocalCredentials = stored

  override fun save(credentials: LocalCredentials) {
    stored = credentials
    saveCalls += 1
  }

  override fun loadFromSecureStorage(): LocalCredentials? = secureStorageCredentials

  override fun isSecureStorageAvailable(): Boolean = secureStorageAvailable
}
