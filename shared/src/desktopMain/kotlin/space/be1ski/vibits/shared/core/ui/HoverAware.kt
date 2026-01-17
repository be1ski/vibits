package space.be1ski.vibits.shared.core.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

/**
 * Desktop hover handling for charts.
 */
@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.hoverAware(onHoverChange: (Boolean) -> Unit): Modifier =
  this
    .onPointerEvent(PointerEventType.Enter) { onHoverChange(true) }
    .onPointerEvent(PointerEventType.Exit) { onHoverChange(false) }
