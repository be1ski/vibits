package space.be1ski.vibits.feature.memos.presentation.view
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.format_active_since
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.feature.habits.domain.parseHabitConfigLine
import space.be1ski.vibits.feature.habits.presentation.view.components.localizedLabel
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostTags

@Composable
internal fun HabitsConfigCard(
  memo: Memo,
  dateFormatter: DateFormatter,
  demoMode: Boolean = false,
) {
  val timeZone = TimeZone.currentSystemDefault()
  val habits =
    memo.content
      .lineSequence()
      .map { it.trim() }
      .filter { it.isNotBlank() }
      .filterNot { it.startsWith(PostTags.HABITS_CONFIG) || it.startsWith(PostTags.HABITS_CONFIG_ALT) }
      .mapNotNull { line -> parseHabitConfigLine(line) }
      .distinctBy { it.tag }
      .toList()

  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    val instant = memo.createTime
    if (instant != null) {
      val date = instant.toLocalDateTime(timeZone).date
      Text(
        text = stringResource(Res.string.format_active_since, dateFormatter.monthDayYear(date)),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    habits.forEach { habit ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Indent.s),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier
              .size(16.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color(habit.color)),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = habit.localizedLabel(demoMode),
            style = MaterialTheme.typography.bodyLarge,
          )
          Text(
            text = habit.tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
