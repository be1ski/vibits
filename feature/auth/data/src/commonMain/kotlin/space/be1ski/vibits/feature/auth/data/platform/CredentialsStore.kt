package space.be1ski.vibits.feature.auth.data.platform

import space.be1ski.vibits.feature.auth.data.LocalCredentials

interface CredentialsStore {
  fun load(): LocalCredentials

  fun save(credentials: LocalCredentials)
}

expect fun createCredentialsStore(): CredentialsStore
