package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private const val MILLIS_PER_DAY = 86400000L

/**
 * Pure reducer for the Memos feature.
 */
val memosReducer: Reducer<MemosAction, MemosState, MemosEffect> =
  reducer { action, state ->
    when (action) {
      // Credentials input
      is MemosAction.UpdateBaseUrl -> {
        state { copy(baseUrl = action.value, errorMessage = null) }
      }

      is MemosAction.UpdateToken -> {
        state { copy(token = action.value, errorMessage = null) }
      }

      is MemosAction.EditCredentials -> {
        state { copy(credentialsMode = true, errorMessage = null) }
        effect(MemosEffect.LoadCredentials)
      }

      is MemosAction.CredentialsLoaded -> {
        state { copy(baseUrl = action.baseUrl, token = action.token) }
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

      is MemosAction.ResetForModeChange -> {
        state {
          copy(
            memos = emptyList(),
            memosRevision = memosRevision + 1,
            initialDataLoaded = false,
            isLoading = false,
          )
        }
      }

      // Filtering
      is MemosAction.ChangePostFilter -> {
        state { copy(activePostFilter = action.filter) }
      }

      is MemosAction.CachedMemosLoaded -> {
        if (state.memos.isNotEmpty()) {
          // Already have memos, don't overwrite with cache
          return@reducer
        }

        if (action.memos.isNotEmpty()) {
          // Cache has data, use it
          state {
            copy(
              memos = sortedMemos(action.memos),
              memosRevision = memosRevision + 1,
              initialDataLoaded = true,
            )
          }
        } else if (!state.isOfflineMode) {
          // Cache is empty and we're online - load from server immediately
          state { copy(isLoading = true) }
          effect(MemosEffect.LoadRemoteMemos)
        } else {
          // Offline mode with no cache - mark as loaded
          state { copy(initialDataLoaded = true) }
        }
      }

      is MemosAction.MemosLoaded -> {
        state {
          copy(
            memos = sortedMemos(action.memos),
            memosRevision = memosRevision + 1,
            isLoading = false,
            errorMessage = null,
            initialDataLoaded = true,
          )
        }
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
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
      }

      is MemosAction.MemoUpdated -> {
        val updatedMemos =
          sortedMemos(
            state.memos.map { memo ->
              if (memo.name == action.memo.name) action.memo else memo
            },
          )
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
      }

      is MemosAction.MemoDeleted -> {
        val updatedMemos = sortedMemos(state.memos.filterNot { it.name == action.name })
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
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

private fun sortedMemos(memos: List<Memo>): List<Memo> {
  return memos.sortedByDescending { memo ->
    // For habit tracking posts, use the tracked date instead of creation date
    val trackingDate =
      space.be1ski.vibits.shared.feature.habits.domain.usecase
        .parseDailyDateFromContent(memo.content)
    if (trackingDate != null) {
      // Convert LocalDate to epoch days, then to milliseconds
      // toEpochDays returns days since 1970-01-01
      trackingDate.toEpochDays() * MILLIS_PER_DAY
    } else {
      // For all other posts, use createTime or updateTime
      memo.createTime?.toEpochMilliseconds()
        ?: memo.updateTime?.toEpochMilliseconds()
        ?: Long.MIN_VALUE
    }
  }
}
