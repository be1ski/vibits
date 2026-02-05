package space.be1ski.vibits.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import space.be1ski.vibits.core.platform.logging.LogLevel
import space.be1ski.vibits.core.utils.logging.LogEntry

private const val LOG_TIMESTAMP_LENGTH = 8
private val MaxHeight = 400.dp
private val ItemSpacing = 4.dp
private val ItemPadding = 6.dp
private val CornerRadius = 4.dp
private val FontSize = 11.sp

/**
 * Reusable log viewer component that displays a list of log entries.
 */
@Composable
fun LogViewer(
  logs: List<LogEntry>,
  emptyMessage: String,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(max = MaxHeight),
    verticalArrangement = Arrangement.spacedBy(ItemSpacing),
  ) {
    items(logs) { entry ->
      LogEntryItem(entry)
    }
    if (logs.isEmpty()) {
      item {
        Text(
          emptyMessage,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun LogEntryItem(entry: LogEntry) {
  val bgColor =
    when (entry.level) {
      LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer
      LogLevel.WARN -> MaterialTheme.colorScheme.tertiaryContainer
      else -> MaterialTheme.colorScheme.surfaceVariant
    }
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(bgColor, RoundedCornerShape(CornerRadius))
        .padding(ItemPadding),
  ) {
    val time = entry.timestamp.substringAfter('T').take(LOG_TIMESTAMP_LENGTH)
    val header = "$time ${entry.level.name.first()}/${entry.tag}"
    Text(
      text = header,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = entry.message,
      style =
        MaterialTheme.typography.bodySmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = FontSize,
        ),
    )
  }
}
