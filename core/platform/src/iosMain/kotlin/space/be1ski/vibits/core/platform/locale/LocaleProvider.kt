package space.be1ski.vibits.core.platform.locale

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

/**
 * iOS implementation.
 * Requires restart for changes to take effect.
 */
actual class LocaleProvider {
  actual fun getSystemLocale(): String = NSLocale.currentLocale.languageCode

  actual fun configureLocale(language: AppLanguage): Boolean {
    // iOS requires restart to apply language changes
    // The preference is stored and will be applied on next launch
    return language != AppLanguage.SYSTEM
  }
}
