package space.be1ski.vibits.feature.onboarding.presentation.view
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
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_continue
import space.be1ski.vibits.core.strings.generated.label_habit_preset_custom
import space.be1ski.vibits.core.strings.generated.title_choose_starter_habit
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.theme.AppColors
import space.be1ski.vibits.core.ui.theme.resolve
import space.be1ski.vibits.feature.habits.domain.model.DemoHabits
import space.be1ski.vibits.feature.habits.presentation.view.components.localizedDemoHabitName
import space.be1ski.vibits.feature.onboarding.domain.model.CUSTOM_PRESET_ID
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

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
  onSelectPreset: (presetId: String, localizedName: String) -> Unit,
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
        val localizedName = preset.localizedName()
        PresetCard(
          preset = preset,
          localizedName = localizedName,
          isSelected = selectedPresetId == preset.id,
          onClick = { onSelectPreset(preset.id, localizedName) },
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
  localizedName: String,
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
        text = localizedName,
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
    CUSTOM_PRESET_ID -> Icons.Default.AutoAwesome
    else -> null
  }
