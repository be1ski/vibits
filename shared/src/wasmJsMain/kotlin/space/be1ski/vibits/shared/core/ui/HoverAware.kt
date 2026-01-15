package space.be1ski.vibits.shared.core.ui

import androidx.compose.ui.Modifier

/**
 * Hover is not supported for wasm web builds.
 */
actual fun Modifier.hoverAware(onHoverChange: (Boolean) -> Unit): Modifier = this
