package space.be1ski.vibits.shared.feature.memos.presentation.view
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
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
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.feature.habits.domain.extractCompletedHabits
import space.be1ski.vibits.shared.feature.habits.domain.extractDateFromTrackingContent
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.view.components.localizedLabel
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

@Composable
internal fun HabitsTrackingCard(
  memo: Memo,
  allMemos: List<Memo>,
  dateFormatter: DateFormatter,
  demoMode: Boolean = false,
) {
  val timeZone = TimeZone.currentSystemDefault()

  // Extract date from tracking memo content
  val trackedDate = extractDateFromTrackingContent(memo.content)
  if (trackedDate == null) {
    // Fallback to plain text if date cannot be extracted
    val instant = memo.createTime ?: memo.updateTime
    if (instant != null) {
      val dateTime = instant.toLocalDateTime(timeZone)
      val dateLabel = dateFormatter.dateTime(dateTime)
      if (dateLabel.isNotBlank()) {
        Text(dateLabel, style = MaterialTheme.typography.labelSmall)
      }
    }
    Text(memo.content, style = MaterialTheme.typography.bodyMedium)
    return
  }

  // Get active config for this date
  val configEntries = ExtractHabitsConfigUseCase(allMemos, timeZone)
  val activeConfig = ExtractHabitsConfigUseCase.forDate(configEntries, trackedDate)?.habits ?: emptyList()

  if (activeConfig.isEmpty()) {
    // Fallback to plain text if no config found
    Text(dateFormatter.monthDay(trackedDate), style = MaterialTheme.typography.labelSmall)
    Text(memo.content, style = MaterialTheme.typography.bodyMedium)
    return
  }

  // Extract completed habits from content
  val completedTags = extractCompletedHabits(memo.content, activeConfig.map { it.tag }.toSet())
  val completedHabits = activeConfig.filter { it.tag in completedTags }

  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    // Date header with progress
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = dateFormatter.monthDay(trackedDate),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "${completedHabits.size}/${activeConfig.size}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Completed habits list
    if (completedHabits.isNotEmpty()) {
      completedHabits.forEach { habit ->
        CompletedHabitRow(habit = habit, demoMode = demoMode)
      }
    } else {
      Text(
        text = "No habits tracked",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun CompletedHabitRow(
  habit: HabitConfig,
  demoMode: Boolean,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(Indent.s),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Filled.CheckCircle,
      contentDescription = null,
      tint = Color(habit.color),
      modifier = Modifier.size(20.dp),
    )
    Text(
      text = habit.localizedLabel(demoMode),
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}
