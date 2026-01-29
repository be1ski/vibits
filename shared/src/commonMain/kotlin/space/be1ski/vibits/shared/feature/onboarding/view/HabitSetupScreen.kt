package space.be1ski.vibits.shared.feature.onboarding.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_start_tracking
import space.be1ski.vibits.shared.generated.demo_habit_early_sleep
import space.be1ski.vibits.shared.generated.demo_habit_exercise
import space.be1ski.vibits.shared.generated.demo_habit_learning
import space.be1ski.vibits.shared.generated.demo_habit_meditation
import space.be1ski.vibits.shared.generated.demo_habit_no_sugar
import space.be1ski.vibits.shared.generated.demo_habit_reading
import space.be1ski.vibits.shared.generated.demo_habit_walking
import space.be1ski.vibits.shared.generated.demo_habit_water
import space.be1ski.vibits.shared.generated.hint_habit_name
import space.be1ski.vibits.shared.generated.label_habit_name
import space.be1ski.vibits.shared.generated.msg_habit_name_required
import space.be1ski.vibits.shared.generated.msg_habit_setup
import space.be1ski.vibits.shared.generated.title_habit_setup

@Suppress("LongMethod")
@Composable
fun HabitSetupScreen(
  selectedPresetId: String?,
  presets: List<HabitPreset>,
  habitName: String,
  isCreating: Boolean,
  error: String?,
  onUpdateName: (String) -> Unit,
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
    "demo_habit_exercise" -> stringResource(Res.string.demo_habit_exercise)
    "demo_habit_water" -> stringResource(Res.string.demo_habit_water)
    "demo_habit_reading" -> stringResource(Res.string.demo_habit_reading)
    "demo_habit_meditation" -> stringResource(Res.string.demo_habit_meditation)
    "demo_habit_walking" -> stringResource(Res.string.demo_habit_walking)
    "demo_habit_learning" -> stringResource(Res.string.demo_habit_learning)
    "demo_habit_no_sugar" -> stringResource(Res.string.demo_habit_no_sugar)
    "demo_habit_early_sleep" -> stringResource(Res.string.demo_habit_early_sleep)
    else -> ""
  }
