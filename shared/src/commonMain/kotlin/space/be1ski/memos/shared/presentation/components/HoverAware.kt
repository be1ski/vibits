package space.be1ski.memos.shared.presentation.components

import androidx.compose.ui.Modifier

/**
 * Adds hover callbacks when supported by the platform.
 */
expect fun Modifier.hoverAware(onHoverChange: (Boolean) -> Unit): Modifier
