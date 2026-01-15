package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.app.AppDetails

/**
 * Provides application details for settings and diagnostics.
 */
expect class AppDetailsProvider() {
  /**
   * Returns app details including version, environment, and storage paths.
   */
  fun load(): AppDetails
}
