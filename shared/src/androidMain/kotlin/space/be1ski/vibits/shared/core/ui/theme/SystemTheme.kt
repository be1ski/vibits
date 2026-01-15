package space.be1ski.vibits.shared.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

@Composable
actual fun rememberSystemDarkTheme(): Boolean = isSystemInDarkTheme()
