package space.be1ski.vibits.core.platform.app

data class AppDetails(
  val version: String,
  val environment: String,
  val credentialsStore: String,
  val memosDatabase: String,
  val offlineStorage: String,
)
