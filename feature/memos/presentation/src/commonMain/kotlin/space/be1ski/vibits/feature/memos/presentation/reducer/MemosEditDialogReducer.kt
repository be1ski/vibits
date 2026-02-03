package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

internal val editDialogReducer: Reducer<MemosAction.EditDialog, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.EditDialog.ShowEditDialog -> {
        state {
          state.copy(
            showEditDialog = true,
            editDialogContent = action.memo.content,
            editDialogMemo = action.memo,
          )
        }
      }

      is MemosAction.EditDialog.UpdateEditContent -> {
        state { state.copy(editDialogContent = action.content) }
      }

      is MemosAction.EditDialog.DismissEditDialog -> {
        state { state.copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null) }
      }

      is MemosAction.EditDialog.ConfirmEditDialog -> {
        val memo = state.editDialogMemo
        val content = state.editDialogContent.trim()
        if (memo != null && content.isNotBlank()) {
          state { state.copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null, isLoading = true) }
          command(MemosEffect.UpdateMemo(memo.name, content))
        }
      }
    }
  }
