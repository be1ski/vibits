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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_continue
import space.be1ski.vibits.shared.generated.label_habit_preset_custom
import space.be1ski.vibits.shared.generated.label_habit_preset_read
import space.be1ski.vibits.shared.generated.label_habit_preset_stretch
import space.be1ski.vibits.shared.generated.label_habit_preset_walk
import space.be1ski.vibits.shared.generated.label_habit_preset_water
import space.be1ski.vibits.shared.generated.title_choose_starter_habit

@Composable
fun ChoosePresetScreen(
  selectedPresetId: String?,
  onSelectPreset: (String) -> Unit,
  onContinue: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val presets =
    listOf(
      PresetItem("water", Res.string.label_habit_preset_water, "💧"),
      PresetItem("stretch", Res.string.label_habit_preset_stretch, "🧘"),
      PresetItem("read", Res.string.label_habit_preset_read, "📚"),
      PresetItem("walk", Res.string.label_habit_preset_walk, "🚶"),
      PresetItem("custom", Res.string.label_habit_preset_custom, "✨"),
    )

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
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
      }
      Text(
        text = stringResource(Res.string.title_choose_starter_habit),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.weight(1f),
      )
    }

    Spacer(modifier = Modifier.height(Indent.m))

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(Indent.s),
    ) {
      items(presets) { preset ->
        PresetCard(
          preset = preset,
          isSelected = selectedPresetId == preset.id,
          onClick = { onSelectPreset(preset.id) },
        )
      }
    }

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
  preset: PresetItem,
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
      Text(
        text = preset.icon,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.size(40.dp),
      )

      Spacer(modifier = Modifier.size(Indent.m))

      Text(
        text = stringResource(preset.nameRes),
        style = MaterialTheme.typography.bodyLarge,
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

private data class PresetItem(
  val id: String,
  val nameRes: org.jetbrains.compose.resources.StringResource,
  val icon: String,
)
