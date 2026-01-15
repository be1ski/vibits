package space.be1ski.vibits.shared.core.platform

import java.util.Locale

/**
 * Desktop platform flag.
 */
actual val isDesktop: Boolean = true

/**
 * Returns the current system language code.
 */
actual fun currentLanguage(): String = Locale.getDefault().language
