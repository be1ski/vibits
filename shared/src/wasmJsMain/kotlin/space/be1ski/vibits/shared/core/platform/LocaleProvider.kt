package space.be1ski.vibits.shared.core.platform

import kotlinx.browser.window
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppLanguage

/**
 * Web implementation.
 * Requires page reload for changes to take effect.
 */
actual class LocaleProvider {
  actual fun getSystemLocale(): String = window.navigator.language.substringBefore("-")

  actual fun configureLocale(language: AppLanguage): Boolean {
    // Web requires page reload to apply language changes
    return language != AppLanguage.SYSTEM
  }
}
