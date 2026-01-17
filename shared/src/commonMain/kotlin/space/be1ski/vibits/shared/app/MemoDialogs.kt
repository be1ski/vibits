package space.be1ski.vibits.shared.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_cancel
import space.be1ski.vibits.shared.action_create
import space.be1ski.vibits.shared.action_save
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.hint_write_memo
import space.be1ski.vibits.shared.title_edit_memo
import space.be1ski.vibits.shared.title_new_memo

@Composable
internal fun MemoCreateDialog(
  appState: VibitsAppUiState,
  dispatch: (MemosAction) -> Unit,
) {
  if (!appState.showCreateMemoDialog) {
    return
  }
  AlertDialog(
    onDismissRequest = {
      appState.showCreateMemoDialog = false
      appState.createMemoContent = ""
    },
    title = { Text(stringResource(Res.string.title_new_memo)) },
    text = {
      TextField(
        value = appState.createMemoContent,
        onValueChange = { appState.createMemoContent = it },
        placeholder = { Text(stringResource(Res.string.hint_write_memo)) },
        modifier = Modifier.fillMaxWidth(),
      )
    },
    confirmButton = {
      val content = appState.createMemoContent.trim()
      Button(
        onClick = {
          dispatch(MemosAction.CreateMemo(content))
          appState.showCreateMemoDialog = false
          appState.createMemoContent = ""
        },
        enabled = content.isNotBlank(),
      ) {
        Text(stringResource(Res.string.action_create))
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          appState.showCreateMemoDialog = false
          appState.createMemoContent = ""
        },
      ) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

@Composable
internal fun MemoEditDialog(
  appState: VibitsAppUiState,
  dispatch: (MemosAction) -> Unit,
) {
  if (!appState.showEditMemoDialog) {
    return
  }
  val memo = appState.editMemoTarget ?: return
  AlertDialog(
    onDismissRequest = { clearMemoEdit(appState) },
    title = { Text(stringResource(Res.string.title_edit_memo)) },
    text = {
      TextField(
        value = appState.editMemoContent,
        onValueChange = { appState.editMemoContent = it },
        modifier = Modifier.fillMaxWidth(),
      )
    },
    confirmButton = {
      val content = appState.editMemoContent.trim()
      Button(
        onClick = {
          dispatch(MemosAction.UpdateMemo(memo.name, content))
          clearMemoEdit(appState)
        },
        enabled = content.isNotBlank(),
      ) {
        Text(stringResource(Res.string.action_save))
      }
    },
    dismissButton = {
      TextButton(onClick = { clearMemoEdit(appState) }) {
        Text(stringResource(Res.string.action_cancel))
      }
    },
  )
}

internal fun beginEditMemo(
  appState: VibitsAppUiState,
  memo: Memo,
) {
  appState.editMemoTarget = memo
  appState.editMemoContent = memo.content
  appState.showEditMemoDialog = true
}

private fun clearMemoEdit(appState: VibitsAppUiState) {
  appState.showEditMemoDialog = false
  appState.editMemoTarget = null
  appState.editMemoContent = ""
}
