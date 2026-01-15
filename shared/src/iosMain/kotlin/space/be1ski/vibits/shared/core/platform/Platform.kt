package space.be1ski.vibits.shared.core.platform

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

/**
 * iOS platform flag.
 */
actual val isDesktop: Boolean = false

/**
 * Returns the current system language code.
 */
actual fun currentLanguage(): String = NSLocale.currentLocale.languageCode
