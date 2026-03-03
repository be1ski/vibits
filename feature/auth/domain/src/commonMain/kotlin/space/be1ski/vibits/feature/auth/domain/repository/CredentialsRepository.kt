package space.be1ski.vibits.feature.auth.domain.repository

import space.be1ski.vibits.feature.auth.domain.model.Credentials

interface CredentialsRepository {
  fun load(): Credentials

  fun save(credentials: Credentials)

  fun loadFromSecureStorage(): Credentials?

  fun isSecureStorageAvailable(): Boolean
}
