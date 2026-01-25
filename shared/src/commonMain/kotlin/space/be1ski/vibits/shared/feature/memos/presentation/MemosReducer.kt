package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Pure reducer for the Memos feature.
 */
val memosReducer: Reducer<MemosAction, MemosState, MemosEffect> =
  reducer { action, state ->
    when (action) {
      // Credentials input
      is MemosAction.UpdateBaseUrl -> {
        state { copy(content = content.copy(baseUrl = action.value), errorMessage = null) }
      }

      is MemosAction.UpdateToken -> {
        state { copy(content = content.copy(token = action.value), errorMessage = null) }
      }

      is MemosAction.EditCredentials -> {
        state { copy(credentialsMode = true, errorMessage = null) }
        effect(MemosEffect.LoadCredentials)
      }

      is MemosAction.CredentialsLoaded -> {
        state { copy(content = content.copy(baseUrl = action.baseUrl, token = action.token)) }
      }

      // Loading
      is MemosAction.LoadMemos -> {
        if (state.needsCredentials) {
          state { copy(credentialsMode = true, errorMessage = "Base URL and token are required.") }
        } else {
          state { copy(isLoading = true, errorMessage = null, credentialsMode = false) }
          if (!state.isOfflineMode) {
            effect(MemosEffect.SaveCredentials(state.baseUrl, state.token))
          }
          effect(MemosEffect.LoadRemoteMemos)
        }
      }

      is MemosAction.LoadCachedMemos -> {
        effect(MemosEffect.LoadCachedMemos)
      }

      // Filtering
      is MemosAction.ChangePostFilter -> {
        state { copy(content = content.copy(activePostFilter = action.filter)) }
      }

      is MemosAction.CachedMemosLoaded -> {
        if (state.memos.isEmpty() && action.memos.isNotEmpty()) {
          state { copy(content = content.copy(memos = sortedMemos(action.memos))) }
        }
      }

      is MemosAction.MemosLoaded -> {
        state { copy(content = content.copy(memos = sortedMemos(action.memos)), isLoading = false, errorMessage = null) }
      }

      // CRUD
      is MemosAction.CreateMemo -> {
        state { copy(isLoading = true) }
        effect(MemosEffect.CreateMemo(action.content))
      }

      is MemosAction.UpdateMemo -> {
        state { copy(isLoading = true) }
        effect(MemosEffect.UpdateMemo(action.name, action.content))
      }

      is MemosAction.DeleteMemo -> {
        state { copy(isLoading = true) }
        effect(MemosEffect.DeleteMemo(action.name))
      }

      is MemosAction.MemoCreated -> {
        val updatedMemos = sortedMemos(state.memos + action.memo)
        state { copy(content = content.copy(memos = updatedMemos), isLoading = false) }
      }

      is MemosAction.MemoUpdated -> {
        val updatedMemos =
          sortedMemos(
            state.memos.map { memo ->
              if (memo.name == action.memo.name) action.memo else memo
            },
          )
        state { copy(content = content.copy(memos = updatedMemos), isLoading = false) }
      }

      is MemosAction.MemoDeleted -> {
        val updatedMemos = sortedMemos(state.memos.filterNot { it.name == action.name })
        state { copy(content = content.copy(memos = updatedMemos), isLoading = false) }
      }

      is MemosAction.OperationFailed -> {
        state { copy(isLoading = false, errorMessage = action.error) }
      }

      // Create dialog
      is MemosAction.ShowCreateDialog -> {
        state { copy(showCreateDialog = true, createDialogContent = "") }
      }

      is MemosAction.UpdateCreateContent -> {
        state { copy(createDialogContent = action.content) }
      }

      is MemosAction.DismissCreateDialog -> {
        state { copy(showCreateDialog = false, createDialogContent = "") }
      }

      is MemosAction.ConfirmCreateDialog -> {
        val content = state.createDialogContent.trim()
        if (content.isNotBlank()) {
          state { copy(showCreateDialog = false, createDialogContent = "", isLoading = true) }
          effect(MemosEffect.CreateMemo(content))
        }
      }

      // Edit dialog
      is MemosAction.ShowEditDialog -> {
        state {
          copy(
            showEditDialog = true,
            editDialogContent = action.memo.content,
            editDialogMemo = action.memo,
          )
        }
      }

      is MemosAction.UpdateEditContent -> {
        state { copy(editDialogContent = action.content) }
      }

      is MemosAction.DismissEditDialog -> {
        state { copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null) }
      }

      is MemosAction.ConfirmEditDialog -> {
        val memo = state.editDialogMemo
        val content = state.editDialogContent.trim()
        if (memo != null && content.isNotBlank()) {
          state { copy(showEditDialog = false, editDialogContent = "", editDialogMemo = null, isLoading = true) }
          effect(MemosEffect.UpdateMemo(memo.name, content))
        }
      }
    }
  }

private fun sortedMemos(memos: List<Memo>): List<Memo> =
  memos.sortedByDescending { memo ->
    memo.createTime?.toEpochMilliseconds()
      ?: memo.updateTime?.toEpochMilliseconds()
      ?: Long.MIN_VALUE
  }
