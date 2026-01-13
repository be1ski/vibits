package space.be1ski.memos.shared.domain.model.auth

/**
 * Domain model for stored server credentials.
 */
data class Credentials(
  val baseUrl: String,
  val token: String
)
