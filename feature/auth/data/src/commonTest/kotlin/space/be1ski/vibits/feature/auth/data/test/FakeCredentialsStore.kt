package space.be1ski.vibits.feature.auth.data.test

import space.be1ski.vibits.feature.auth.data.LocalCredentials
import space.be1ski.vibits.feature.auth.data.platform.CredentialsStore

class FakeCredentialsStore(
  initial: LocalCredentials = LocalCredentials(baseUrl = "", token = ""),
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
}
