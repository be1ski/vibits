package space.be1ski.vibits.shared.core.ui

import androidx.compose.ui.Modifier

/**
 * Hover events are not available on Android; return the modifier unchanged.
 */
actual fun Modifier.hoverAware(@Suppress("unused") onHoverChange: (Boolean) -> Unit): Modifier = this
