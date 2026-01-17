package space.be1ski.vibits.shared.core.platform

import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import java.util.Locale

/**
 * Desktop implementation using Locale.setDefault().
 * Requires restart for changes to take effect.
 */
actual class LocaleProvider {
  actual fun getSystemLocale(): String = Locale.getDefault().language

  actual fun configureLocale(language: AppLanguage): Boolean {
    val locale = language.localeCode?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    Locale.setDefault(locale)
    return true
  }
}
