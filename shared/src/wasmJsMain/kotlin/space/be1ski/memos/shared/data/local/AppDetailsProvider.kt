package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.app.AppDetails

/**
 * Web implementation.
 */
actual class AppDetailsProvider {
  actual fun load(): AppDetails = AppDetails(
    version = "web",
    environment = "web",
    credentialsStore = "in-memory",
    memosDatabase = "in-memory",
    offlineStorage = "localStorage"
  )
}
