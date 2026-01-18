package space.be1ski.vibits.shared.core.platform

import kotlinx.browser.window
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage

/**
 * Web implementation.
 * Uses window.__customLocale to override navigator.languages at runtime.
 */
actual class LocaleProvider {
  actual fun getSystemLocale(): String = window.navigator.language.substringBefore("-")

  actual fun configureLocale(language: AppLanguage): Boolean {
    setCustomLocale(language.localeCode)
    // Return false - no restart needed on web with the navigator override
    return false
  }
}

@Suppress("UNUSED_PARAMETER")
private fun setCustomLocale(locale: String?) {
  js("window.__customLocale = locale")
}
