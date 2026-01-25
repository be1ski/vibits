package space.be1ski.vibits.shared.core.platform.env

/**
 * Provides access to local configuration (local.properties, environment variables, etc).
 */
fun interface LocalConfigProvider {
  /**
   * Reads a configuration value by key.
   * Returns null if the key is not set.
   */
  fun get(key: String): String?
}

/**
 * Creates platform-specific LocalConfigProvider.
 */
expect fun createLocalConfigProvider(): LocalConfigProvider
