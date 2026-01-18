package space.be1ski.vibits.shared.core.platform.app

import space.be1ski.vibits.shared.app.domain.model.AppDetails

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
