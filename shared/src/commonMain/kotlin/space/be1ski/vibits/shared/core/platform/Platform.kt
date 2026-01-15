package space.be1ski.vibits.shared.core.platform

/**
 * True when running on desktop.
 */
expect val isDesktop: Boolean

/**
 * Returns the current system language code (e.g., "en", "ru").
 */
expect fun currentLanguage(): String
