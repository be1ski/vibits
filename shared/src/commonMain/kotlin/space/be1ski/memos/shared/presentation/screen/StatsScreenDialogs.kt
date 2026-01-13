package space.be1ski.memos.shared.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.stringResource
import space.be1ski.memos.shared.Res
import space.be1ski.memos.shared.action_cancel
import space.be1ski.memos.shared.action_create
import space.be1ski.memos.shared.title_create_day
import space.be1ski.memos.shared.action_delete
import space.be1ski.memos.shared.msg_delete_day_confirm
import space.be1ski.memos.shared.title_delete_day
import space.be1ski.memos.shared.presentation.components.Indent
import space.be1ski.memos.shared.action_update
import space.be1ski.memos.shared.title_update_day

@Composable
internal fun HabitEditorDialog(derived: StatsScreenDerivedState) {
  val uiState = derived.uiState
  if (uiState.habitsEditorDay == null) {
    return
  }
  val isEditing = uiState.habitsEditorExisting != null
  AlertDialog(
    onDismissRequest = {
      uiState.habitsEditorDay = null
      uiState.habitsEditorExisting = null
    },
    title = { Text(if (isEditing) stringResource(Res.string.title_update_day) else stringResource(Res.string.title_create_day)) },
    text = { HabitEditorContent(uiState) },
    confirmButton = { HabitEditorConfirmButton(derived) },
    dismissButton = { HabitEditorDismissButton(derived) }
  )
}

@Composable
private fun HabitEditorContent(uiState: StatsScreenUiState) {
  Column(verticalArrangement = Arrangement.spacedBy(Indent.xs)) {
    if (uiState.habitsEditorConfig.isNotEmpty()) {
      uiState.habitsEditorConfig.forEach { habit ->
        val tag = habit.tag
        val done = uiState.habitsEditorSelections[tag] == true
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = done,
            onCheckedChange = { checked ->
              uiState.habitsEditorSelections = uiState.habitsEditorSelections.toMutableMap().also { it[tag] = checked }
            }
          )
          Text(habit.label, style = MaterialTheme.typography.bodySmall)
        }
      }
    } else {
      uiState.habitsEditorSelections.forEach { (tag, done) ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = done,
            onCheckedChange = { checked ->
              uiState.habitsEditorSelections = uiState.habitsEditorSelections.toMutableMap().also { it[tag] = checked }
            }
          )
          Text(tag, style = MaterialTheme.typography.bodySmall)
        }
      }
    }
  }
  uiState.habitsEditorError?.let { message ->
    Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
  }
}

@Composable
private fun HabitEditorConfirmButton(derived: StatsScreenDerivedState) {
  val uiState = derived.uiState
  val isEditing = uiState.habitsEditorExisting != null
  Button(
    onClick = {
      val hasSelection = uiState.habitsEditorSelections.values.any { it }
      if (!hasSelection) {
        if (uiState.habitsEditorExisting != null) {
          uiState.showEmptyDeleteConfirm = true
        } else {
          uiState.habitsEditorError = "Select at least one habit." // This will be handled in StatsScreenContent or here
          // Actually, let's use the resource for the error too if possible, but it's a dynamic message usually.
          // In this case it's hardcoded.
          uiState.habitsEditorError = "Select at least one habit."
        }
        return@Button
      }
      val day = uiState.habitsEditorDay
      if (day != null) {
        val content = buildDailyContent(day.date, uiState.habitsEditorConfig, uiState.habitsEditorSelections)
        val existing = uiState.habitsEditorExisting
        if (existing != null) {
          derived.actions.onEditDailyMemo(existing, content)
        } else {
          derived.actions.onCreateDailyMemo(content)
        }
      }
      uiState.selectedDate = null
      uiState.selectedWeek = null
      uiState.activeSelectionId = null
      uiState.habitsEditorDay = null
      uiState.habitsEditorExisting = null
      uiState.habitsEditorError = null
    }
  ) {
    Text(if (isEditing) stringResource(Res.string.action_update) else stringResource(Res.string.action_create))
  }
}

@Composable
private fun HabitEditorDismissButton(derived: StatsScreenDerivedState) {
  val uiState = derived.uiState
  val isEditing = uiState.habitsEditorExisting != null
  Row(horizontalArrangement = Arrangement.spacedBy(Indent.xs)) {
    if (isEditing) {
      TextButton(onClick = {
        val existing = uiState.habitsEditorExisting
        if (existing != null) {
          derived.actions.onDeleteDailyMemo(existing)
        }
        uiState.selectedDate = null
        uiState.selectedWeek = null
        uiState.activeSelectionId = null
        uiState.habitsEditorDay = null
        uiState.habitsEditorExisting = null
        uiState.habitsEditorError = null
      }) {
        Text(stringResource(Res.string.action_delete))
      }
    }
    TextButton(onClick = {
      uiState.habitsEditorDay = null
      uiState.habitsEditorExisting = null
      uiState.habitsEditorError = null
    }) {
      Text(stringResource(Res.string.action_cancel))
    }
  }
}

@Composable
internal fun EmptyDeleteDialog(derived: StatsScreenDerivedState) {
  val uiState = derived.uiState
  if (!uiState.showEmptyDeleteConfirm) {
    return
  }
  AlertDialog(
    onDismissRequest = { uiState.showEmptyDeleteConfirm = false },
    title = { Text(stringResource(Res.string.title_delete_day)) },
    text = { Text(stringResource(Res.string.msg_delete_day_confirm)) },
    confirmButton = {
      Button(
        onClick = {
          val existing = uiState.habitsEditorExisting
          if (existing != null) {
            derived.actions.onDeleteDailyMemo(existing)
          }
          uiState.selectedDate = null
          uiState.selectedWeek = null
          uiState.activeSelectionId = null
          uiState.habitsEditorDay = null
          uiState.habitsEditorExisting = null
          uiState.habitsEditorError = null
          uiState.showEmptyDeleteConfirm = false
        }
      ) {
        Text(stringResource(Res.string.action_delete))
      }
    },
    dismissButton = {
      TextButton(onClick = { uiState.showEmptyDeleteConfirm = false }) {
        Text(stringResource(Res.string.action_cancel))
      }
    }
  )
}
