package space.be1ski.memos.shared.feature.habits.presentation.components

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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.memos.shared.Res
import space.be1ski.memos.shared.action_cancel
import space.be1ski.memos.shared.action_save
import space.be1ski.memos.shared.core.ui.Indent
import space.be1ski.memos.shared.feature.habits.domain.model.HABIT_COLORS
import space.be1ski.memos.shared.feature.habits.presentation.EditableHabit
import space.be1ski.memos.shared.feature.habits.presentation.HabitsAction
import space.be1ski.memos.shared.feature.habits.presentation.HabitsState
import space.be1ski.memos.shared.label_habits_config
import space.be1ski.memos.shared.hint_habit_name

private val COLOR_CIRCLE_SIZE = 24.dp
private val SELECTED_BORDER_WIDTH = 2.dp

@Composable
internal fun HabitsConfigDialog(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit
) {
  if (!habitsState.showConfigDialog) {
    return
  }

  AlertDialog(
    onDismissRequest = { dispatch(HabitsAction.CloseConfigDialog) },
    title = { Text(stringResource(Res.string.label_habits_config)) },
    text = { HabitsConfigDialogContent(habitsState, dispatch) },
    confirmButton = {
      Button(onClick = { dispatch(HabitsAction.SaveConfigDialog) }) {
        Text(stringResource(Res.string.action_save))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(HabitsAction.CloseConfigDialog) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    }
  )
}

@Composable
private fun HabitsConfigDialogContent(
  habitsState: HabitsState,
  dispatch: (HabitsAction) -> Unit
) {
  Column(
    verticalArrangement = Arrangement.spacedBy(Indent.s),
    modifier = Modifier.verticalScroll(rememberScrollState())
  ) {
    habitsState.editingHabits.forEach { habit ->
      HabitConfigItem(
        habit = habit,
        onLabelChange = { dispatch(HabitsAction.UpdateHabitLabel(habit.id, it)) },
        onColorChange = { dispatch(HabitsAction.UpdateHabitColor(habit.id, it)) },
        onDelete = { dispatch(HabitsAction.DeleteHabit(habit.id)) }
      )
    }

    TextButton(
      onClick = { dispatch(HabitsAction.AddHabit) },
      modifier = Modifier.fillMaxWidth()
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
  onLabelChange: (String) -> Unit,
  onColorChange: (Long) -> Unit,
  onDelete: () -> Unit
) {
  OutlinedCard(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(Indent.s),
      verticalArrangement = Arrangement.spacedBy(Indent.xs)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Indent.xs)
      ) {
        Box(
          modifier = Modifier
            .size(COLOR_CIRCLE_SIZE)
            .clip(CircleShape)
            .background(Color(habit.color))
        )
        TextField(
          value = habit.label,
          onValueChange = onLabelChange,
          modifier = Modifier.weight(1f),
          placeholder = { Text(stringResource(Res.string.hint_habit_name)) },
          singleLine = true
        )
        IconButton(onClick = onDelete) {
          Icon(
            Icons.Filled.Delete,
            contentDescription = stringResource(Res.string.action_cancel),
            tint = MaterialTheme.colorScheme.error
          )
        }
      }

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Indent.xs),
        verticalArrangement = Arrangement.spacedBy(Indent.xs)
      ) {
        HABIT_COLORS.forEach { color ->
          ColorCircle(
            color = color,
            isSelected = habit.color == color,
            onClick = { onColorChange(color) }
          )
        }
      }
    }
  }
}

@Composable
private fun ColorCircle(
  color: Long,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val borderColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    Color.Transparent
  }

  Box(
    modifier = Modifier
      .size(COLOR_CIRCLE_SIZE)
      .clip(CircleShape)
      .background(Color(color))
      .border(SELECTED_BORDER_WIDTH, borderColor, CircleShape)
      .clickable(onClick = onClick)
  )
}
