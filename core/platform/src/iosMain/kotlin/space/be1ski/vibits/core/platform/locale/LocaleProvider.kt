package space.be1ski.vibits.core.platform.locale

/**
 * iOS implementation.
 * Requires restart for changes to take effect.
 */
actual class LocaleProvider {
  actual fun configureLocale(language: AppLanguage): Boolean {
    // iOS requires restart to apply language changes
    // The preference is stored and will be applied on next launch
    return language != AppLanguage.SYSTEM
  }
}
