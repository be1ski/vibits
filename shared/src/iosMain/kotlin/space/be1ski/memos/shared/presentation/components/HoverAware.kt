package space.be1ski.memos.shared.presentation.components

import androidx.compose.ui.Modifier

/**
 * Hover events are not available on iOS; return the modifier unchanged.
 */
actual fun Modifier.hoverAware(onHoverChange: (Boolean) -> Unit): Modifier = this
