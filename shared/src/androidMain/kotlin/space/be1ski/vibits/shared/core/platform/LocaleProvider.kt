package space.be1ski.vibits.shared.core.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import java.util.Locale

/**
 * Android implementation using AppCompatDelegate for per-app language settings.
 */
actual class LocaleProvider {
  private val originalSystemLocale: Locale = Locale.getDefault()

  actual fun getSystemLocale(): String = originalSystemLocale.language

  actual fun configureLocale(language: AppLanguage): Boolean {
    val locale = language.localeCode?.let { Locale.forLanguageTag(it) } ?: originalSystemLocale
    Locale.setDefault(locale)

    val localeList =
      language.localeCode
        ?.let { LocaleListCompat.forLanguageTags(it) }
        ?: LocaleListCompat.getEmptyLocaleList()
    AppCompatDelegate.setApplicationLocales(localeList)
    return false
  }
}
