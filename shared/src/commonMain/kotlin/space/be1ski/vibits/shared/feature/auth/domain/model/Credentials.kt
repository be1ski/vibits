package space.be1ski.vibits.shared.feature.auth.domain.model

/**
 * Domain model for stored server credentials.
 */
data class Credentials(
  val baseUrl: String,
  val token: String
)
