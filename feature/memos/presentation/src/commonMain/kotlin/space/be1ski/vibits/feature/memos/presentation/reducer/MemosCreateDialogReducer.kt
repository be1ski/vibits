package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

internal val createDialogReducer: Reducer<MemosAction.CreateDialog, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.CreateDialog.ShowCreateDialog -> {
        state { state.copy(showCreateDialog = true, createDialogContent = "") }
      }

      is MemosAction.CreateDialog.UpdateCreateContent -> {
        state { state.copy(createDialogContent = action.content) }
      }

      is MemosAction.CreateDialog.DismissCreateDialog -> {
        state { state.copy(showCreateDialog = false, createDialogContent = "") }
      }

      is MemosAction.CreateDialog.ConfirmCreateDialog -> {
        val content = state.createDialogContent.trim()
        if (content.isNotBlank()) {
          state { state.copy(showCreateDialog = false, createDialogContent = "", isLoading = true) }
          command(MemosEffect.CreateMemo(content))
        }
      }
    }
  }
