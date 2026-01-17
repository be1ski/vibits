package space.be1ski.vibits.shared.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A reusable segmented selector component for choosing between options.
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
          Text(optionLabel(option))
        }
      }
    }
  }
}
