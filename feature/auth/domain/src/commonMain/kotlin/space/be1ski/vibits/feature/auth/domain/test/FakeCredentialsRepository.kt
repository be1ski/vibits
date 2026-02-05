package space.be1ski.vibits.feature.auth.domain.test

import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository

class FakeCredentialsRepository(
  initial: Credentials = Credentials(baseUrl = "", token = ""),
) : CredentialsRepository {
  var stored: Credentials = initial
    private set
  var saveCount: Int = 0
    private set

  override fun load(): Credentials = stored

  override fun save(credentials: Credentials) {
    stored = credentials
    saveCount += 1
  }
}
