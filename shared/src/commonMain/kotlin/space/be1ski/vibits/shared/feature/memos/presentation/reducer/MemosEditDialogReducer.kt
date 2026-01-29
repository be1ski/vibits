package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

internal val editDialogReducer: Reducer<MemosAction.EditDialog, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.EditDialog.ShowEditDialog -> {
        state {
          copy(
            showEditDialog = true,
            editDialogContent = action.memo.content,
            editDialogMemo = action.memo,
          )
        }
      }

      is MemosAction.EditDialog.UpdateEditContent -> {
        state { copy(editDialogContent = action.content) }
      }

      is MemosAction.EditDialog.DismissEditDialog -> {
        state { copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null) }
      }

      is MemosAction.EditDialog.ConfirmEditDialog -> {
        val memo = state.editDialogMemo
        val content = state.editDialogContent.trim()
        if (memo != null && content.isNotBlank()) {
          state { copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null, isLoading = true) }
          command(MemosEffect.UpdateMemo(memo.name, content))
        }
      }
    }
  }
