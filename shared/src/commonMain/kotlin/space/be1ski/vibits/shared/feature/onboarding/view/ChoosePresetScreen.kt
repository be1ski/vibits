package space.be1ski.vibits.shared.feature.onboarding.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.habits.domain.model.DemoHabits
import space.be1ski.vibits.shared.feature.habits.view.components.localizedDemoHabitName
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_continue
import space.be1ski.vibits.shared.generated.label_habit_preset_custom
import space.be1ski.vibits.shared.generated.title_choose_starter_habit

@Composable
private fun HabitPreset.localizedName(): String =
  when (nameKey) {
    "label_habit_preset_custom" -> stringResource(Res.string.label_habit_preset_custom)
    else -> localizedDemoHabitName(nameKey)
  }

@Composable
fun ChoosePresetScreen(
  presets: List<HabitPreset>,
  selectedPresetId: String?,
  onSelectPreset: (String) -> Unit,
  onContinue: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val textColor = AppColors.onBackground.resolve()

  Column(
    modifier =
      modifier
        .padding(Indent.xl),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
      }
      Text(
        text = stringResource(Res.string.title_choose_starter_habit),
        style = MaterialTheme.typography.headlineMedium,
        color = textColor,
        modifier = Modifier.weight(1f),
      )
    }

    Spacer(modifier = Modifier.height(Indent.m))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(Indent.s),
    ) {
      items(presets, key = { it.id }) { preset ->
        PresetCard(
          preset = preset,
          isSelected = selectedPresetId == preset.id,
          onClick = { onSelectPreset(preset.id) },
        )
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    Spacer(modifier = Modifier.height(Indent.m))

    Button(
      onClick = onContinue,
      enabled = selectedPresetId != null,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(stringResource(Res.string.action_continue))
    }
  }
}

@Composable
private fun PresetCard(
  preset: HabitPreset,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val backgroundColor =
    if (isSelected) {
      AppColors.cardSelected.resolve()
    } else {
      AppColors.cardBackground.resolve()
    }
  val textColor = AppColors.onCardBackground.resolve()
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(Indent.s),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(Indent.m),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      preset.iconVector()?.let { icon ->
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = textColor,
          modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.size(Indent.m))
      }

      Text(
        text = preset.localizedName(),
        style = MaterialTheme.typography.bodyLarge,
        color = textColor,
        modifier = Modifier.weight(1f),
      )

      if (isSelected) {
        Icon(
          Icons.Default.Check,
          contentDescription = "Selected",
          tint = AppColors.habitBlue.resolve(),
        )
      }
    }
  }
}

private fun HabitPreset.iconVector(): ImageVector? =
  when (id) {
    DemoHabits.EXERCISE -> Icons.Default.FitnessCenter
    DemoHabits.WATER -> Icons.Default.WaterDrop
    DemoHabits.READING -> Icons.AutoMirrored.Filled.MenuBook
    DemoHabits.MEDITATION -> Icons.Default.SelfImprovement
    DemoHabits.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    DemoHabits.LEARNING -> Icons.Default.School
    DemoHabits.NO_SUGAR -> Icons.Default.NoFood
    DemoHabits.EARLY_SLEEP -> Icons.Default.Bedtime
    "custom" -> Icons.Default.AutoAwesome
    else -> null
  }
