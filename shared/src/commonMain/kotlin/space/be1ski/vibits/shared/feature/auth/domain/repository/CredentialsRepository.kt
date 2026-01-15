package space.be1ski.vibits.shared.feature.auth.domain.repository

import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials

/**
 * Repository for reading and persisting user credentials.
 */
interface CredentialsRepository {
  /**
   * Loads stored credentials or empty values.
   */
  fun load(): Credentials

  /**
   * Persists credentials locally.
   */
  fun save(credentials: Credentials)
}
