package space.be1ski.vibits.feature.auth.domain.model

/**
 * Domain model for stored server credentials.
 */
data class Credentials(
  val baseUrl: String,
  val token: String,
)
