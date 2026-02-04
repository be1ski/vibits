package space.be1ski.vibits.feature.auth.domain.model

/**
 * Domain model for stored server credentials.
 */
data class Credentials(
  val baseUrl: String,
  val token: String,
)

val Credentials.isFilled: Boolean
  get() = baseUrl.trim().isNotBlank() && token.trim().isNotBlank()

fun Credentials.trimmed(): Credentials =
  Credentials(
    baseUrl = baseUrl.trim(),
    token = token.trim(),
  )

fun Credentials.requireFilled(): Credentials {
  check(isFilled) { "Base URL and token are required." }
  return trimmed()
}
