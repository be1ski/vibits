package space.be1ski.vibits.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun ExpandableDetails(
  toggleLabel: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Column(modifier = modifier) {
    Text(
      text = toggleLabel,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.clickable { expanded = !expanded },
    )
    AnimatedVisibility(visible = expanded) {
      content()
    }
  }
}
