package space.be1ski.vibits.feature.habits.presentation.view.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.day_fri
import space.be1ski.vibits.core.strings.generated.day_mon
import space.be1ski.vibits.core.strings.generated.day_sat
import space.be1ski.vibits.core.strings.generated.day_sun
import space.be1ski.vibits.core.strings.generated.day_thu
import space.be1ski.vibits.core.strings.generated.day_tue
import space.be1ski.vibits.core.strings.generated.day_wed
import space.be1ski.vibits.core.strings.generated.format_weekday_completion_rate
import space.be1ski.vibits.core.strings.generated.label_weekday_performance
import space.be1ski.vibits.core.strings.generated.msg_not_enough_data_for_trends
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.habits.presentation.state.WeekdayPerformanceCardState
import space.be1ski.vibits.feature.habits.presentation.state.WeekdayPerformanceStats
import kotlin.math.roundToInt

private val BAR_MAX_HEIGHT = 64.dp
private val BAR_MIN_HEIGHT = 2.dp
private val BAR_CONTAINER_WIDTH = 24.dp
private val BAR_WIDTH = 16.dp
private const val BAR_ANIM_MS = 320
private const val COLOR_ANIM_MS = 180
private const val PERCENT_FACTOR = 100

@Composable
internal fun WeekdayPerformanceCard(
  state: WeekdayPerformanceCardState,
  modifier: Modifier = Modifier,
) {
  val dayLabels =
    listOf(
      stringResource(Res.string.day_mon),
      stringResource(Res.string.day_tue),
      stringResource(Res.string.day_wed),
      stringResource(Res.string.day_thu),
      stringResource(Res.string.day_fri),
      stringResource(Res.string.day_sat),
      stringResource(Res.string.day_sun),
    )

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    Text(
      text = stringResource(Res.string.label_weekday_performance),
      style = MaterialTheme.typography.titleSmall,
    )
    if (state.hasSufficientData) {
      WeekdayBarsWithAvgLine(
        stats = state.stats,
        dayLabels = dayLabels,
        averageCompletionRate =
          checkNotNull(state.averageCompletionRate) {
            "averageCompletionRate must be non-null when hasSufficientData is true"
          },
      )
    } else {
      WeekdayBarsNoHighlight(stats = state.stats, dayLabels = dayLabels)
      Spacer(Modifier.height(Indent.x3s))
      Text(
        text = stringResource(Res.string.msg_not_enough_data_for_trends),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun WeekdayBarsWithAvgLine(
  stats: List<WeekdayPerformanceStats>,
  dayLabels: List<String>,
  averageCompletionRate: Float,
) {
  val accent = MaterialTheme.colorScheme.primary
  val muted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
  val neutral = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
  val avgLineColor = MaterialTheme.colorScheme.outline

  Box(modifier = Modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      stats.forEachIndexed { index, stat ->
        WeekdayBar(
          stat = stat,
          label = dayLabels[index],
          accentColor = accent,
          mutedColor = muted,
          neutralColor = neutral,
          withHighlight = true,
        )
      }
    }
    val avgFraction = averageCompletionRate.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.matchParentSize()) {
      val barMaxPx = BAR_MAX_HEIGHT.toPx()
      val lineY = barMaxPx * (1f - avgFraction)
      drawLine(
        color = avgLineColor,
        start = Offset(0f, lineY),
        end = Offset(size.width, lineY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f),
      )
    }
  }
}

@Composable
private fun WeekdayBarsNoHighlight(
  stats: List<WeekdayPerformanceStats>,
  dayLabels: List<String>,
) {
  val neutral = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    stats.forEachIndexed { index, stat ->
      WeekdayBar(
        stat = stat,
        label = dayLabels[index],
        accentColor = neutral,
        mutedColor = neutral,
        neutralColor = neutral,
        withHighlight = false,
      )
    }
  }
}

@Composable
private fun WeekdayBar(
  stat: WeekdayPerformanceStats,
  label: String,
  accentColor: Color,
  mutedColor: Color,
  neutralColor: Color,
  withHighlight: Boolean,
) {
  var targetHeight by remember { mutableStateOf(BAR_MIN_HEIGHT) }
  LaunchedEffect(stat.completionRate) {
    targetHeight = (BAR_MAX_HEIGHT * stat.completionRate).coerceAtLeast(BAR_MIN_HEIGHT)
  }
  val barHeight by animateDpAsState(targetValue = targetHeight, animationSpec = tween(BAR_ANIM_MS))

  val targetColor =
    when {
      withHighlight && stat.isBest -> accentColor
      withHighlight && stat.isWorst -> mutedColor
      else -> neutralColor
    }
  val barColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(COLOR_ANIM_MS))

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Indent.x3s),
  ) {
    Box(
      modifier = Modifier.size(width = BAR_CONTAINER_WIDTH, height = BAR_MAX_HEIGHT),
      contentAlignment = Alignment.BottomCenter,
    ) {
      Box(
        modifier =
          Modifier
            .size(width = BAR_WIDTH, height = barHeight)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(barColor),
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = stringResource(Res.string.format_weekday_completion_rate, (stat.completionRate * PERCENT_FACTOR).roundToInt()),
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
