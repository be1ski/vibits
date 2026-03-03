package space.be1ski.vibits.feature.auth.data.platform

import space.be1ski.vibits.feature.auth.data.LocalCredentials

interface CredentialsStore {
  fun load(): LocalCredentials

  fun save(credentials: LocalCredentials)

  fun loadFromSecureStorage(): LocalCredentials? = null

  fun isSecureStorageAvailable(): Boolean = false
}

expect fun createCredentialsStore(): CredentialsStore
