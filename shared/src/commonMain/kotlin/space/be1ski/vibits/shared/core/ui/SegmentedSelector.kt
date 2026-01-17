package space.be1ski.vibits.shared.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

/**
 * Text that automatically scales down to fit within its container.
 * Prevents text from wrapping to multiple lines by reducing font size.
 */
@Composable
private fun AutoSizeText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  minFontSize: Float = 10f,
) {
  var scaledStyle by remember(text, style) { mutableStateOf(style) }
  var readyToDraw by remember(text, style) { mutableStateOf(false) }

  Text(
    text = text,
    modifier = modifier.drawWithContent {
      if (readyToDraw) drawContent()
    },
    style = scaledStyle,
    maxLines = 1,
    softWrap = false,
    overflow = TextOverflow.Clip,
    onTextLayout = { result ->
      if (result.didOverflowWidth) {
        val currentSize = scaledStyle.fontSize
        val size = if (currentSize.isUnspecified) 14f else currentSize.value
        if (size > minFontSize) {
          scaledStyle = scaledStyle.copy(fontSize = (size - 1f).sp)
        } else {
          readyToDraw = true
        }
      } else {
        readyToDraw = true
      }
    },
  )
}

/**
 * A reusable segmented selector component for choosing between options.
 * Uses auto-sizing text to prevent overflow on small screens.
 *
 * @param label The label displayed above the selector
 * @param options List of options to display
 * @param selected Currently selected option
 * @param onSelect Callback when an option is selected
 * @param optionLabel Composable function to get display label for each option
 * @param enabled Whether the selector is enabled
 */
@Suppress("LongParameterList")
@Composable
fun <T> SegmentedSelector(
  label: String,
  options: List<T>,
  selected: T,
  onSelect: (T) -> Unit,
  optionLabel: @Composable (T) -> String,
  enabled: Boolean = true,
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(label)
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
      options.forEachIndexed { index, option ->
        SegmentedButton(
          selected = option == selected,
          onClick = { onSelect(option) },
          enabled = enabled,
          shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
        ) {
          AutoSizeText(optionLabel(option))
        }
      }
    }
  }
}
