package space.be1ski.vibits.core.ui.platform.theme

import androidx.compose.runtime.Composable

/**
 * Configures system bars (status bar, navigation bar) based on the current theme.
 * On Android, this updates the status bar icons to match light/dark theme.
 * On other platforms, this is a no-op.
 */
@Composable
expect fun ConfigureSystemBars(darkTheme: Boolean)
