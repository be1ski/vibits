package space.be1ski.vibits.shared.core.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import java.util.Locale

/**
 * Android implementation using AppCompatDelegate for per-app language settings.
 */
actual class LocaleProvider {
  actual fun getSystemLocale(): String = Locale.getDefault().language

  actual fun configureLocale(language: AppLanguage): Boolean {
    val locale =
      when (language) {
        AppLanguage.SYSTEM -> Locale.getDefault()
        AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
        AppLanguage.RUSSIAN -> Locale.forLanguageTag("ru")
      }
    // Set default locale for Compose Resources
    Locale.setDefault(locale)

    // Also use AppCompatDelegate for Android per-app language
    val localeList =
      when (language) {
        AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
        AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
        AppLanguage.RUSSIAN -> LocaleListCompat.forLanguageTags("ru")
      }
    AppCompatDelegate.setApplicationLocales(localeList)
    return false
  }
}
