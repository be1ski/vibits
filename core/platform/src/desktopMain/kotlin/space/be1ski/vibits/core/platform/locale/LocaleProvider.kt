package space.be1ski.vibits.core.platform.locale

import java.util.Locale

/**
 * Desktop implementation using Locale.setDefault().
 * Requires restart for changes to take effect.
 */
actual class LocaleProvider {
  private val originalSystemLocale: Locale = Locale.getDefault()

  actual fun getSystemLocale(): String = originalSystemLocale.language

  actual fun configureLocale(language: AppLanguage): Boolean {
    val locale = language.localeCode?.let { Locale.forLanguageTag(it) } ?: originalSystemLocale
    Locale.setDefault(locale)
    return true
  }
}
