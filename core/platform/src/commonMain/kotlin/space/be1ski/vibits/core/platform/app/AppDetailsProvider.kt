package space.be1ski.vibits.core.platform.app

/**
 * Provides application details for settings and diagnostics.
 */
expect class AppDetailsProvider() {
  /**
   * Returns app details including version, environment, and storage paths.
   */
  fun load(): AppDetails
}
