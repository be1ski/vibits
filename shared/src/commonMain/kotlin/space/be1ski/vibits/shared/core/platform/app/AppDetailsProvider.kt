package space.be1ski.vibits.shared.core.platform.app

import space.be1ski.vibits.shared.app.domain.model.AppDetails

/**
 * Provides application details for settings and diagnostics.
 */
expect class AppDetailsProvider() {
  /**
   * Returns app details including version, environment, and storage paths.
   */
  fun load(): AppDetails
}
