package space.be1ski.vibits.shared.core.platform

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppLanguage

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
