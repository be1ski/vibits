package space.be1ski.memos.shared.data.local

/** DTO for persisted credentials. */
data class LocalCredentials(
  val baseUrl: String,
  val token: String
)

/**
 * Platform-specific credential storage.
 */
expect class CredentialsStore() {
  /**
   * Loads saved credentials or empty values when not available.
   */
  fun load(): LocalCredentials

  /**
   * Persists credentials after a successful load.
   */
  fun save(credentials: LocalCredentials)
}
