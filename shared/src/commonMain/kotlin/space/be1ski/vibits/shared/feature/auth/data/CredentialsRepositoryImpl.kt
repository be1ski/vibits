package space.be1ski.vibits.shared.feature.auth.data

import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository

/**
 * Repository implementation backed by platform credential storage.
 */
class CredentialsRepositoryImpl(
  private val credentialsStore: CredentialsStore
) : CredentialsRepository {
  /**
   * Loads stored credentials from the platform store.
   */
  override fun load(): Credentials {
    val local = credentialsStore.load()
    return Credentials(baseUrl = local.baseUrl, token = local.token)
  }

  /**
   * Persists credentials to the platform store.
   */
  override fun save(credentials: Credentials) {
    credentialsStore.save(LocalCredentials(baseUrl = credentials.baseUrl, token = credentials.token))
  }
}
