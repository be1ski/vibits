package space.be1ski.vibits.shared.app.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_cancel
import space.be1ski.vibits.shared.generated.action_create
import space.be1ski.vibits.shared.generated.action_save
import space.be1ski.vibits.shared.generated.hint_write_memo
import space.be1ski.vibits.shared.generated.title_edit_memo
import space.be1ski.vibits.shared.generated.title_new_memo

@Composable
internal fun MemoCreateDialog(
  state: MemosState,
  dispatch: (MemosAction) -> Unit,
) {
  if (!state.showCreateDialog) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(MemosAction.DismissCreateDialog) },
    title = { Text(stringResource(Res.string.title_new_memo)) },
    text = {
      TextField(
        value = state.createDialogContent,
        onValueChange = { dispatch(MemosAction.UpdateCreateContent(it)) },
        placeholder = { Text(stringResource(Res.string.hint_write_memo)) },
        modifier = Modifier.fillMaxWidth(),
      )
    },
    confirmButton = {
      Button(
        onClick = { dispatch(MemosAction.ConfirmCreateDialog) },
        enabled = state.createDialogContent.trim().isNotBlank(),
      ) {
        Text(stringResource(Res.string.action_create))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(MemosAction.DismissCreateDialog) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Composable
internal fun MemoEditDialog(
  state: MemosState,
  dispatch: (MemosAction) -> Unit,
) {
  if (!state.showEditDialog) {
    return
  }
  AlertDialog(
    onDismissRequest = { dispatch(MemosAction.DismissEditDialog) },
    title = { Text(stringResource(Res.string.title_edit_memo)) },
    text = {
      TextField(
        value = state.editDialogContent,
        onValueChange = { dispatch(MemosAction.UpdateEditContent(it)) },
        modifier = Modifier.fillMaxWidth(),
      )
    },
    confirmButton = {
      Button(
        onClick = { dispatch(MemosAction.ConfirmEditDialog) },
        enabled = state.editDialogContent.trim().isNotBlank(),
      ) {
        Text(stringResource(Res.string.action_save))
      }
    },
    dismissButton = {
      TextButton(onClick = { dispatch(MemosAction.DismissEditDialog) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}
