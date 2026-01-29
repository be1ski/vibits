package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

internal fun editDialogReducer(
  action: MemosAction.EditDialog,
  state: MemosState,
): ReducerResult<MemosState, MemosEffect, Nothing> =
  reducer<MemosAction.EditDialog, MemosState, MemosEffect, Nothing> { a, s ->
    when (a) {
      is MemosAction.EditDialog.ShowEditDialog -> {
        state {
          copy(
            showEditDialog = true,
            editDialogContent = a.memo.content,
            editDialogMemo = a.memo,
          )
        }
      }

      is MemosAction.EditDialog.UpdateEditContent -> {
        state { copy(editDialogContent = a.content) }
      }

      is MemosAction.EditDialog.DismissEditDialog -> {
        state { copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null) }
      }

      is MemosAction.EditDialog.ConfirmEditDialog -> {
        val memo = s.editDialogMemo
        val content = s.editDialogContent.trim()
        if (memo != null && content.isNotBlank()) {
          state { copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null, isLoading = true) }
          command(MemosEffect.UpdateMemo(memo.name, content))
        }
      }
    }
  }(action, state)
