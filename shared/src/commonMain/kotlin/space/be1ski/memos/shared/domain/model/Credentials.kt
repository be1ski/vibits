package space.be1ski.memos.shared.domain.model

/**
 * Domain model for persisted server credentials.
 */
data class Credentials(
  /**
   * Base URL for the Memos server.
   */
  val baseUrl: String,
  /**
   * Access token for API requests.
   */
  val token: String
)
