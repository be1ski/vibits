package space.be1ski.vibits.shared.core.ui

import androidx.compose.ui.Modifier

/**
 * Hover events are not available on Android; return the modifier unchanged.
 */
@Suppress("UNUSED_PARAMETER")
actual fun Modifier.hoverAware(onHoverChange: (Boolean) -> Unit): Modifier = this
