package space.be1ski.memos.shared.domain.model.app

/**
 * Application details for settings and diagnostics.
 */
data class AppDetails(
  val version: String,
  val environment: String,
  val credentialsStore: String,
  val memosDatabase: String,
  val offlineStorage: String
)
