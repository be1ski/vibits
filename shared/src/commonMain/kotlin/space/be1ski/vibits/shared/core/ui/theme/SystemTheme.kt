package space.be1ski.vibits.shared.core.ui.theme

import androidx.compose.runtime.Composable

/**
 * Returns true if the system is in dark theme, with support for
 * dynamic theme changes on all platforms.
 */
@Composable
expect fun rememberSystemDarkTheme(): Boolean
