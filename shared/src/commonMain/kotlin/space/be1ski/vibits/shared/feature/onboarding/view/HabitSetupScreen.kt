package space.be1ski.vibits.shared.feature.onboarding.view

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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.HabitColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.habits.view.components.localizedDemoHabitName
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_start_tracking
import space.be1ski.vibits.shared.generated.hint_habit_name
import space.be1ski.vibits.shared.generated.label_habit_color
import space.be1ski.vibits.shared.generated.label_habit_name
import space.be1ski.vibits.shared.generated.label_habit_preset_custom
import space.be1ski.vibits.shared.generated.msg_habit_name_required
import space.be1ski.vibits.shared.generated.msg_habit_setup
import space.be1ski.vibits.shared.generated.title_habit_setup

private val COLOR_CIRCLE_SIZE = 24.dp
private val SELECTED_BORDER_WIDTH = 2.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitSetupScreen(
  selectedPresetId: String?,
  presets: List<HabitPreset>,
  habitName: String,
  selectedColor: Long,
  isCreating: Boolean,
  error: String?,
  onUpdateName: (String) -> Unit,
  onColorChange: (Long) -> Unit,
  onCreate: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedPreset = presets.find { it.id == selectedPresetId }
  val localizedPresetName =
    selectedPreset?.let {
      if (it.id != "custom") getLocalizedPresetName(it.nameKey) else ""
    } ?: ""

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
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
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
      horizontalArrangement = Arrangement.spacedBy(Indent.xs),
      verticalArrangement = Arrangement.spacedBy(Indent.xs),
    ) {
      HabitColors.forEach { color ->
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
private fun getLocalizedPresetName(nameKey: String): String =
  when (nameKey) {
    "label_habit_preset_custom" -> stringResource(Res.string.label_habit_preset_custom)
    else -> localizedDemoHabitName(nameKey)
  }

@Composable
private fun ColorCircle(
  color: Long,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val borderColor =
    if (isSelected) {
      MaterialTheme.colorScheme.primary
    } else {
      Color.Transparent
    }

  Box(
    modifier =
      Modifier
        .size(COLOR_CIRCLE_SIZE)
        .clip(CircleShape)
        .background(Color(color))
        .border(SELECTED_BORDER_WIDTH, borderColor, CircleShape)
        .clickable(onClick = onClick),
  )
}
