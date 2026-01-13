package space.be1ski.memos.shared.presentation.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import space.be1ski.memos.shared.presentation.components.ActivityRange

@Composable
internal fun ActivityRangeSelector(
  years: List<Int>,
  selectedRange: ActivityRange,
  onRangeChange: (ActivityRange) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  val label = when (selectedRange) {
    is ActivityRange.Last7Days -> "Last 7 days"
    is ActivityRange.Last90Days -> "Last 90 days"
    is ActivityRange.Last6Months -> "Last 6 months"
    is ActivityRange.LastYear -> "Last year"
    is ActivityRange.Year -> selectedRange.year.toString()
  }
  Row(verticalAlignment = Alignment.CenterVertically) {
    TextButton(onClick = { expanded = true }) {
      Text(label)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      DropdownMenuItem(
        text = { Text("Last 7 days") },
        onClick = {
          onRangeChange(ActivityRange.Last7Days)
          expanded = false
        }
      )
      DropdownMenuItem(
        text = { Text("Last 90 days") },
        onClick = {
          onRangeChange(ActivityRange.Last90Days)
          expanded = false
        }
      )
      DropdownMenuItem(
        text = { Text("Last 6 months") },
        onClick = {
          onRangeChange(ActivityRange.Last6Months)
          expanded = false
        }
      )
      DropdownMenuItem(
        text = { Text("Last year") },
        onClick = {
          onRangeChange(ActivityRange.LastYear)
          expanded = false
        }
      )
      years.forEach { year ->
        DropdownMenuItem(
          text = { Text(year.toString()) },
          onClick = {
            onRangeChange(ActivityRange.Year(year))
            expanded = false
          }
        )
      }
    }
  }
}
