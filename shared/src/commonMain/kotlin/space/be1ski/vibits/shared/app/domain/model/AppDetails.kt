package space.be1ski.vibits.shared.app.domain.model

/**
 * Application details for settings and diagnostics.
 */
data class AppDetails(
  val version: String,
  val environment: String,
  val credentialsStore: String,
  val memosDatabase: String,
  val offlineStorage: String,
)
