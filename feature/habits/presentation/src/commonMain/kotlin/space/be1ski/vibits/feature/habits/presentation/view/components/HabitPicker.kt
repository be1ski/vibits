package space.be1ski.vibits.feature.habits.presentation.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.label_all_habits
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig

private val COLOR_DOT_SIZE = 8.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HabitPicker(
  habits: List<HabitConfig>,
  selectedHabitTag: String?,
  demoMode: Boolean,
  onSelect: (String?) -> Unit,
) {
  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
    FlowRow(
      modifier = Modifier.padding(bottom = Indent.xs),
      horizontalArrangement = Arrangement.spacedBy(Indent.xs),
      verticalArrangement = Arrangement.spacedBy(Indent.x3s),
    ) {
      FilterChip(
        selected = selectedHabitTag == null,
        onClick = { onSelect(null) },
        label = { Text(stringResource(Res.string.label_all_habits)) },
        border = null,
        elevation = null,
      )
      habits.forEach { habit ->
        FilterChip(
          selected = selectedHabitTag == habit.tag,
          onClick = { onSelect(habit.tag) },
          label = { Text(habit.localizedLabel(demoMode)) },
          leadingIcon = {
            Box(
              modifier =
                Modifier
                  .size(COLOR_DOT_SIZE)
                  .background(Color(habit.color.argb), CircleShape),
            )
          },
          border = null,
          elevation = null,
        )
      }
    }
  }
}
