package space.be1ski.vibits.core.platform.locale

/**
 * Platform-specific locale configuration.
 */
expect class LocaleProvider() {
  /**
   * Returns the system locale code (e.g., "en", "ru").
   */
  fun getSystemLocale(): String

  /**
   * Configures the app locale based on the selected language.
   * @return true if a restart is required for the change to take effect
   */
  fun configureLocale(language: AppLanguage): Boolean
}
