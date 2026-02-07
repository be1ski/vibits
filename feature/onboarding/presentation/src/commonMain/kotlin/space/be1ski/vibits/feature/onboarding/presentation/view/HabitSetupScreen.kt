package space.be1ski.vibits.feature.onboarding.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_back
import space.be1ski.vibits.core.strings.generated.action_start_tracking
import space.be1ski.vibits.core.strings.generated.hint_habit_name
import space.be1ski.vibits.core.strings.generated.label_habit_color
import space.be1ski.vibits.core.strings.generated.label_habit_name
import space.be1ski.vibits.core.strings.generated.msg_habit_name_required
import space.be1ski.vibits.core.strings.generated.msg_habit_setup
import space.be1ski.vibits.core.strings.generated.title_habit_setup
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.habits.localizedDemoHabitName
import space.be1ski.vibits.core.ui.theme.AppColors
import space.be1ski.vibits.core.ui.theme.ColorPalette
import space.be1ski.vibits.core.ui.theme.resolve
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

private val COLOR_CIRCLE_SIZE = 40.dp
private val SELECTED_BORDER_WIDTH = 3.dp
private val CHECKMARK_SIZE = 20.dp
private const val LUMINANCE_THRESHOLD = 0.5f

@Suppress("LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitSetupScreen(
  selectedPresetId: String?,
  presets: List<HabitPreset>,
  habitName: String,
  selectedColor: HabitColor,
  isCreating: Boolean,
  error: String?,
  onUpdateName: (String) -> Unit,
  onColorChange: (HabitColor) -> Unit,
  onCreate: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedPreset = presets.find { it.id == selectedPresetId }
  val localizedPresetName =
    selectedPreset?.demoHabit?.let { localizedDemoHabitName(it) } ?: ""

  // Auto-fill habit name from localized preset name
  LaunchedEffect(selectedPresetId) {
    if (habitName.isEmpty() && localizedPresetName.isNotEmpty()) {
      onUpdateName(localizedPresetName)
    }
  }

  val errorMessage = resolveErrorMessage(error)
  val textColor = AppColors.onBackground.resolve()

  Column(
    modifier =
      modifier
        .padding(Indent.xl),
    verticalArrangement = Arrangement.spacedBy(Indent.m),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      IconButton(onClick = onBack, enabled = !isCreating) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back), tint = textColor)
      }
      Text(
        text = stringResource(Res.string.title_habit_setup),
        style = MaterialTheme.typography.headlineMedium,
        color = textColor,
        modifier = Modifier.weight(1f),
      )
    }

    Text(
      text = stringResource(Res.string.msg_habit_setup),
      style = MaterialTheme.typography.bodyMedium,
      color = textColor,
    )

    Spacer(modifier = Modifier.height(Indent.s))

    TextField(
      value = habitName,
      onValueChange = onUpdateName,
      label = { Text(stringResource(Res.string.label_habit_name)) },
      placeholder = { Text(stringResource(Res.string.hint_habit_name)) },
      enabled = !isCreating,
      isError = error != null,
      supportingText =
        errorMessage?.let {
          { Text(it, color = AppColors.errorColor.resolve()) }
        },
      modifier = Modifier.fillMaxWidth(),
    )

    Text(
      text = stringResource(Res.string.label_habit_color),
      style = MaterialTheme.typography.bodyMedium,
      color = textColor,
    )

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(Indent.s),
      verticalArrangement = Arrangement.spacedBy(Indent.s),
    ) {
      ColorPalette.forEach { colorLong ->
        val color = HabitColor(colorLong)
        ColorCircle(
          color = color,
          isSelected = selectedColor == color,
          onClick = { if (!isCreating) onColorChange(color) },
        )
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    Button(
      onClick = onCreate,
      enabled = !isCreating && habitName.isNotBlank(),
      modifier = Modifier.fillMaxWidth(),
    ) {
      if (isCreating) {
        CircularProgressIndicator(
          modifier = Modifier.padding(end = Indent.xs),
        )
      }
      Text(stringResource(Res.string.action_start_tracking))
    }
  }
}

@Composable
private fun resolveErrorMessage(error: String?): String? =
  error?.let { errorKey ->
    when (errorKey) {
      "habit_name_required" -> stringResource(Res.string.msg_habit_name_required)
      else -> errorKey
    }
  }

@Composable
private fun ColorCircle(
  color: HabitColor,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val circleColor = Color(color.argb)
  val borderColor =
    if (isSelected) {
      MaterialTheme.colorScheme.primary
    } else {
      Color.Transparent
    }
  val checkmarkColor =
    if (circleColor.luminance() > LUMINANCE_THRESHOLD) {
      Color.Black
    } else {
      Color.White
    }

  Box(
    modifier =
      Modifier
        .size(COLOR_CIRCLE_SIZE)
        .clip(CircleShape)
        .background(circleColor)
        .border(SELECTED_BORDER_WIDTH, borderColor, CircleShape)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    if (isSelected) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = checkmarkColor,
        modifier = Modifier.size(CHECKMARK_SIZE),
      )
    }
  }
}
