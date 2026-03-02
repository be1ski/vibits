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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_back
import space.be1ski.vibits.core.strings.generated.action_continue
import space.be1ski.vibits.core.strings.generated.label_habit_preset_custom
import space.be1ski.vibits.core.strings.generated.label_selected
import space.be1ski.vibits.core.strings.generated.title_choose_starter_habit
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.habits.localizedDemoHabitName
import space.be1ski.vibits.core.ui.theme.AppColors
import space.be1ski.vibits.core.ui.theme.resolve
import space.be1ski.vibits.core.utils.habits.DemoHabit
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

@Composable
private fun HabitPreset.localizedName(): String =
  demoHabit?.let { localizedDemoHabitName(it) }
    ?: stringResource(Res.string.label_habit_preset_custom)

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
        .testTag(OnboardingTestTags.CHOOSE_PRESET_SCREEN)
        .padding(Indent.xl),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      IconButton(onClick = onBack, modifier = Modifier.testTag(OnboardingTestTags.CHOOSE_PRESET_BACK_BUTTON)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back), tint = textColor)
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
      verticalArrangement = Arrangement.spacedBy(Indent.xs),
      modifier = Modifier.weight(1f),
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

    Spacer(modifier = Modifier.height(Indent.s))

    Button(
      onClick = onContinue,
      enabled = selectedPresetId != null,
      modifier = Modifier.fillMaxWidth().testTag(OnboardingTestTags.CHOOSE_PRESET_CONTINUE_BUTTON),
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
          contentDescription = stringResource(Res.string.label_selected),
          tint = AppColors.habitBlue.resolve(),
        )
      }
    }
  }
}

private fun HabitPreset.iconVector(): ImageVector? =
  when (demoHabit) {
    DemoHabit.EXERCISE -> Icons.Default.FitnessCenter
    DemoHabit.WATER -> Icons.Default.WaterDrop
    DemoHabit.READING -> Icons.AutoMirrored.Filled.MenuBook
    DemoHabit.MEDITATION -> Icons.Default.SelfImprovement
    DemoHabit.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
    DemoHabit.LEARNING -> Icons.Default.School
    DemoHabit.NO_SUGAR -> Icons.Default.NoFood
    DemoHabit.EARLY_SLEEP -> Icons.Default.Bedtime
    null -> Icons.Default.AutoAwesome
  }
