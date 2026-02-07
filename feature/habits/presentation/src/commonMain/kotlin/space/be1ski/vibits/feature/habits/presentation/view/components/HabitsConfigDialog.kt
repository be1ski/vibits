package space.be1ski.vibits.feature.habits.presentation.view.components
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_cancel
import space.be1ski.vibits.core.strings.generated.action_delete
import space.be1ski.vibits.core.strings.generated.action_save
import space.be1ski.vibits.core.strings.generated.hint_habit_name
import space.be1ski.vibits.core.strings.generated.label_habits_config
import space.be1ski.vibits.core.strings.generated.msg_delete_config_warning
import space.be1ski.vibits.core.strings.generated.title_delete_config
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.theme.ColorPalette
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.habits.domain.model.demoHabit
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.EditableHabit
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.StatsTestTags

private val COLOR_CIRCLE_SIZE = 24.dp
private val SELECTED_BORDER_WIDTH = 2.dp

@Composable
fun HabitsConfigDialog(
  habitsState: HabitsState,
  demoMode: Boolean = false,
  dispatch: (HabitsAction) -> Unit,
) {
  if (!habitsState.showConfigDialog) {
    return
  }

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.Config.CloseConfigDialog) },
    modifier = Modifier.testTag(StatsTestTags.HABITS_CONFIG_DIALOG),
    title = { Text(stringResource(Res.string.label_habits_config)) },
    text = { HabitsConfigDialogContent(habitsState, demoMode, dispatch) },
    confirmButton = {
      Button(onClick = { dispatch(HabitsAction.Config.SaveConfigDialog) }) {
        Text(stringResource(Res.string.action_save))
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs)) {
        if (habitsState.editingConfigMemo != null) {
          TextButton(
            onClick = { dispatch(HabitsAction.ConfigDelete.RequestDeleteConfig) },
            colors =
              ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
              ),
          ) {
            Text(stringResource(Res.string.action_delete))
          }
        }
        TextButton(onClick = { dispatch(HabitsAction.Config.CloseConfigDialog) }) {
          Text(stringResource(Res.string.action_cancel))
        }
      }
    },
  )

  // Delete confirmation dialog
  if (habitsState.showDeleteConfigConfirm) {
    DeleteConfigConfirmDialog(dispatch = dispatch)
  }
}

@Composable
private fun DeleteConfigConfirmDialog(dispatch: (HabitsAction) -> Unit) {
  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.ConfigDelete.CancelDeleteConfig) },
    title = { Text(stringResource(Res.string.title_delete_config)) },
    text = { Text(stringResource(Res.string.msg_delete_config_warning)) },
    confirmButton = {
      Button(
        onClick = { dispatch(HabitsAction.ConfigDelete.ConfirmDeleteConfig) },
        colors =
          ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
          ),
      ) {
        Text(stringResource(Res.string.action_delete))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(HabitsAction.ConfigDelete.CancelDeleteConfig) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Composable
private fun HabitsConfigDialogContent(
  habitsState: HabitsState,
  demoMode: Boolean,
  dispatch: (HabitsAction) -> Unit,
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Indent.s),
    modifier = Modifier.verticalScroll(rememberScrollState()),
  ) {
    habitsState.editingHabits.forEach { habit ->
      HabitConfigItem(
        habit = habit,
        demoMode = demoMode,
        onLabelChange = { dispatch(HabitsAction.Config.UpdateHabitLabel(habit.id, it)) },
        onColorChange = { dispatch(HabitsAction.Config.UpdateHabitColor(habit.id, it)) },
        onDelete = { dispatch(HabitsAction.Config.DeleteHabit(habit.id)) },
      )
    }

    TextButton(
      onClick = { dispatch(HabitsAction.Config.AddHabit) },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
      Text(stringResource(Res.string.hint_habit_name), modifier = Modifier.padding(start = Indent.xs))
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitConfigItem(
  habit: EditableHabit,
  demoMode: Boolean,
  onLabelChange: (String) -> Unit,
  onColorChange: (HabitColor) -> Unit,
  onDelete: () -> Unit,
) {
  val habitConfig = habit.toHabitConfig()
  val isDemoHabit = demoMode && habitConfig.demoHabit() != null

  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(Indent.s),
      verticalArrangement = Arrangement.spacedBy(Indent.xs),
    ) {
      HabitLabelEditor(
        habit = habit,
        demoMode = demoMode,
        onLabelChange = onLabelChange,
        onDelete = onDelete,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Indent.xs),
        verticalArrangement = Arrangement.spacedBy(Indent.xs),
      ) {
        ColorPalette.forEach { colorLong ->
          val color = HabitColor(colorLong)
          ColorCircle(
            color = color,
            isSelected = habit.color == color,
            onClick = { if (!isDemoHabit) onColorChange(color) },
          )
        }
      }
    }
  }
}

@Composable
private fun HabitLabelEditor(
  habit: EditableHabit,
  demoMode: Boolean,
  onLabelChange: (String) -> Unit,
  onDelete: () -> Unit,
) {
  val habitConfig = habit.toHabitConfig()
  val isDemoHabit = demoMode && habitConfig.demoHabit() != null
  val displayLabel = if (isDemoHabit) habitConfig.localizedLabel(demoMode) else habit.label

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(Indent.xs),
  ) {
    Box(
      modifier =
        Modifier
          .size(COLOR_CIRCLE_SIZE)
          .clip(CircleShape)
          .background(Color(habit.color.argb)),
    )
    TextField(
      value = displayLabel,
      onValueChange = onLabelChange,
      modifier = Modifier.weight(1f),
      placeholder = { Text(stringResource(Res.string.hint_habit_name)) },
      singleLine = true,
      enabled = !isDemoHabit,
    )
    IconButton(onClick = onDelete) {
      Icon(
        Icons.Filled.Delete,
        contentDescription = stringResource(Res.string.action_cancel),
        tint = MaterialTheme.colorScheme.error,
      )
    }
  }
}

@Composable
private fun ColorCircle(
  color: HabitColor,
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
        .background(Color(color.argb))
        .border(SELECTED_BORDER_WIDTH, borderColor, CircleShape)
        .clickable(onClick = onClick),
  )
}
