package space.be1ski.vibits.core.platform.app

/**
 * Web implementation.
 */
actual class AppDetailsProvider {
  actual fun load(): AppDetails =
    AppDetails(
      version = "web",
      environment = "web",
      credentialsStore = "in-memory",
      memosDatabase = "in-memory",
      offlineStorage = "localStorage",
    )
}
