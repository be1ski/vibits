package space.be1ski.vibits.shared.core.platform

import java.util.Locale

/**
 * Android platform flag.
 */
actual val isDesktop: Boolean = false

/**
 * Returns the current system language code.
 */
actual fun currentLanguage(): String = Locale.getDefault().language
