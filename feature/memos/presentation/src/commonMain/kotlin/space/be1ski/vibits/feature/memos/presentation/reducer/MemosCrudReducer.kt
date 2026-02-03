package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

internal val crudReducer: Reducer<MemosAction.Crud, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.Crud.CreateMemo -> {
        state { state.copy(isLoading = true) }
        command(MemosEffect.CreateMemo(action.content))
      }

      is MemosAction.Crud.UpdateMemo -> {
        state { state.copy(isLoading = true) }
        command(MemosEffect.UpdateMemo(action.name, action.content))
      }

      is MemosAction.Crud.DeleteMemo -> {
        state { state.copy(isLoading = true) }
        command(MemosEffect.DeleteMemo(action.name))
      }

      is MemosAction.Crud.MemoCreated -> {
        val updatedMemos = sortedMemos(state.memos + action.memo)
        state { state.copy(memos = updatedMemos, memosRevision = state.memosRevision + 1, isLoading = false) }
        // Trigger sync to push local changes to server in online mode
        if (!state.isOfflineMode) {
          command(MemosEffect.PerformSync)
        }
      }

      is MemosAction.Crud.MemoUpdated -> {
        val updatedMemos =
          sortedMemos(
            state.memos.map { memo ->
              if (memo.name == action.memo.name) action.memo else memo
            },
          )
        state { state.copy(memos = updatedMemos, memosRevision = state.memosRevision + 1, isLoading = false) }
        // Trigger sync to push local changes to server in online mode
        if (!state.isOfflineMode) {
          command(MemosEffect.PerformSync)
        }
      }

      is MemosAction.Crud.MemoDeleted -> {
        val updatedMemos = sortedMemos(state.memos.filterNot { it.name == action.name })
        state { state.copy(memos = updatedMemos, memosRevision = state.memosRevision + 1, isLoading = false) }
        // Trigger sync to push local changes to server in online mode
        if (!state.isOfflineMode) {
          command(MemosEffect.PerformSync)
        }
      }

      is MemosAction.Crud.OperationFailed -> {
        state { state.copy(isLoading = false, errorMessage = action.error) }
      }
    }
  }
