package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_changelog_dismiss
import space.be1ski.vibits.core.strings.generated.title_changelog
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry

private const val GROUP_LINK_TEXT = 1
private const val GROUP_LINK_URL = 2
private const val GROUP_BOLD = 3
private const val GROUP_BARE_URL = 4

private val formattedPattern =
  Regex("""!\[.*?]\(.*?\)|\[(.+?)]\((https?://\S+?)\)|\*\*(.+?)\*\*|(https?://\S+)""")

@Composable
internal fun ChangelogDialog(
  entries: List<ChangelogEntry>,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.title_changelog)) },
    text = {
      Column(
        modifier =
          Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Indent.m),
      ) {
        entries.forEach { entry ->
          ChangelogEntryContent(entry)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.action_changelog_dismiss))
      }
    },
  )
}

@Composable
private fun ChangelogEntryContent(entry: ChangelogEntry) {
  val linkColor = MaterialTheme.colorScheme.primary
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(Indent.xs),
  ) {
    Text(
      text = "${entry.title} (${entry.date})",
      style = MaterialTheme.typography.titleSmall,
    )
    if (entry.body.isNotBlank()) {
      Text(
        text = renderBody(entry.body, linkColor),
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Composable
private fun renderBody(
  body: String,
  linkColor: Color,
) = buildAnnotatedString {
  body.lines().forEach { line ->
    when {
      line.startsWith("## ") -> {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
          append(line.removePrefix("## "))
        }
        append("\n")
      }
      line.startsWith("* ") || line.startsWith("- ") -> {
        append("  ")
        appendFormattedLine(line.drop(2).trimStart(), linkColor)
        append("\n")
      }
      line.isBlank() -> append("\n")
      else -> {
        appendFormattedLine(line, linkColor)
        append("\n")
      }
    }
  }
}

private fun AnnotatedString.Builder.appendFormattedLine(
  text: String,
  linkColor: Color,
) {
  var lastIndex = 0
  formattedPattern.findAll(text).forEach { match ->
    append(text.substring(lastIndex, match.range.first))
    when {
      match.value.startsWith("![") -> Unit
      match.groupValues[GROUP_LINK_TEXT].isNotEmpty() ->
        appendLink(match.groupValues[GROUP_LINK_TEXT], match.groupValues[GROUP_LINK_URL], linkColor)
      match.groupValues[GROUP_BOLD].isNotEmpty() ->
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(match.groupValues[GROUP_BOLD]) }
      match.groupValues[GROUP_BARE_URL].isNotEmpty() ->
        appendLink(match.groupValues[GROUP_BARE_URL], match.groupValues[GROUP_BARE_URL], linkColor)
    }
    lastIndex = match.range.last + 1
  }
  if (lastIndex < text.length) {
    append(text.substring(lastIndex))
  }
}

private fun AnnotatedString.Builder.appendLink(
  text: String,
  url: String,
  linkColor: Color,
) {
  val start = length
  withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
    append(text)
  }
  addLink(LinkAnnotation.Url(url), start, length)
}
