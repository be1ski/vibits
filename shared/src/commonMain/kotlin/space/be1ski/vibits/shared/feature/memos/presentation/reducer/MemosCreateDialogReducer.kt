package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState

internal fun createDialogReducer(
  action: MemosAction.CreateDialog,
  state: MemosState,
): ReducerResult<MemosState, MemosEffect, Nothing> =
  reducer<MemosAction.CreateDialog, MemosState, MemosEffect, Nothing> { a, s ->
    when (a) {
      is MemosAction.CreateDialog.ShowCreateDialog -> {
        state { copy(showCreateDialog = true, createDialogContent = "") }
      }

      is MemosAction.CreateDialog.UpdateCreateContent -> {
        state { copy(createDialogContent = a.content) }
      }

      is MemosAction.CreateDialog.DismissCreateDialog -> {
        state { copy(showCreateDialog = false, createDialogContent = "") }
      }

      is MemosAction.CreateDialog.ConfirmCreateDialog -> {
        val content = s.createDialogContent.trim()
        if (content.isNotBlank()) {
          state { copy(showCreateDialog = false, createDialogContent = "", isLoading = true) }
          command(MemosEffect.CreateMemo(content))
        }
      }
    }
  }(action, state)
