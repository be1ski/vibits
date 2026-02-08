package space.be1ski.vibits.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun StatePanel(
  title: String,
  message: String,
  modifier: Modifier = Modifier,
  icon: @Composable (() -> Unit)? = null,
  primaryActionLabel: String? = null,
  onPrimaryAction: (() -> Unit)? = null,
  secondaryActionLabel: String? = null,
  onSecondaryAction: (() -> Unit)? = null,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    if (icon != null) {
      icon()
      Spacer(modifier = Modifier.height(Indent.l))
    }

    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Indent.xs))

    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )

    if (primaryActionLabel != null && onPrimaryAction != null) {
      Spacer(modifier = Modifier.height(Indent.l))
      Button(
        onClick = onPrimaryAction,
      ) {
        Text(primaryActionLabel)
      }
    }

    if (secondaryActionLabel != null && onSecondaryAction != null) {
      Spacer(modifier = Modifier.height(Indent.xs))
      TextButton(
        onClick = onSecondaryAction,
      ) {
        Text(secondaryActionLabel)
      }
    }
  }
}
