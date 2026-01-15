package space.be1ski.vibits.shared.core.platform

import kotlinx.browser.window

/**
 * Returns true when running on desktop.
 */
actual val isDesktop: Boolean = false

/**
 * Returns the current browser language code.
 */
actual fun currentLanguage(): String = window.navigator.language.substringBefore("-")
