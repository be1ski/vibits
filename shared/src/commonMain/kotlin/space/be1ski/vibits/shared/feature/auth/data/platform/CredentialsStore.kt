package space.be1ski.vibits.shared.feature.auth.data.platform

import space.be1ski.vibits.shared.feature.auth.data.LocalCredentials

expect class CredentialsStore() {
  fun load(): LocalCredentials

  fun save(credentials: LocalCredentials)
}
