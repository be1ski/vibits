package space.be1ski.memos.shared.feature.habits.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val ANIMATION_DURATION_MS = 600
private const val ARC_START_ANGLE = 135f
private const val ARC_SWEEP_ANGLE = 270f
private const val PERCENT_MULTIPLIER = 100

private val INDICATOR_SIZE = 72.dp
private val INDICATOR_PADDING = 8.dp
private val STROKE_WIDTH = 8.dp

private const val GREEN_THRESHOLD = 0.8f
private const val YELLOW_THRESHOLD = 0.5f

private const val COLOR_GREEN_HEX = 0xFF4CAF50
private const val COLOR_YELLOW_HEX = 0xFFFFC107
private const val COLOR_RED_HEX = 0xFFE57373

private val COLOR_GREEN = Color(COLOR_GREEN_HEX)
private val COLOR_YELLOW = Color(COLOR_YELLOW_HEX)
private val COLOR_RED = Color(COLOR_RED_HEX)

@Composable
internal fun SuccessRateIndicator(
  rate: Float,
  periodLabel: String,
  modifier: Modifier = Modifier
) {
  val animatedProgress by animateFloatAsState(
    targetValue = rate,
    animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
    label = "successRateProgress"
  )

  val percentText = "${(animatedProgress * PERCENT_MULTIPLIER).toInt()}%"
  val progressColor = successRateColor(animatedProgress)
  val trackColor = MaterialTheme.colorScheme.surfaceVariant

  Box(
    modifier = modifier.size(INDICATOR_SIZE),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(INDICATOR_PADDING)) {
      val strokeWidth = STROKE_WIDTH.toPx()
      val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
      val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

      drawArc(
        color = trackColor,
        startAngle = ARC_START_ANGLE,
        sweepAngle = ARC_SWEEP_ANGLE,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )

      drawArc(
        color = progressColor,
        startAngle = ARC_START_ANGLE,
        sweepAngle = ARC_SWEEP_ANGLE * animatedProgress,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
      )
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = percentText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = progressColor
      )
      Text(
        text = periodLabel,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun successRateColor(rate: Float): Color {
  return when {
    rate >= GREEN_THRESHOLD -> COLOR_GREEN
    rate >= YELLOW_THRESHOLD -> COLOR_YELLOW
    else -> COLOR_RED
  }
}
