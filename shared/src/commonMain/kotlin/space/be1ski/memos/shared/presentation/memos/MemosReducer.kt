package space.be1ski.memos.shared.presentation.memos

import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.core.elm.Reducer
import space.be1ski.memos.shared.core.elm.reducer

/**
 * Pure reducer for the Memos feature.
 */
val memosReducer: Reducer<MemosAction, MemosState, MemosEffect> = reducer { action, state ->
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
      if (!state.hasCredentials) {
        state { copy(credentialsMode = true, errorMessage = "Base URL and token are required.") }
      } else {
        state { copy(isLoading = true, errorMessage = null, credentialsMode = false) }
        effect(MemosEffect.SaveCredentials(state.baseUrl, state.token))
        effect(MemosEffect.LoadRemoteMemos)
      }
    }

    is MemosAction.LoadCachedMemos -> {
      effect(MemosEffect.LoadCachedMemos)
    }

    is MemosAction.CachedMemosLoaded -> {
      if (state.memos.isEmpty() && action.memos.isNotEmpty()) {
        state { copy(memos = sortedMemos(action.memos)) }
      }
    }

    is MemosAction.MemosLoaded -> {
      state { copy(memos = sortedMemos(action.memos), isLoading = false, errorMessage = null) }
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
      state { copy(memos = updatedMemos, isLoading = false) }
    }

    is MemosAction.MemoUpdated -> {
      val updatedMemos = sortedMemos(state.memos.map { memo ->
        if (memo.name == action.memo.name) action.memo else memo
      })
      state { copy(memos = updatedMemos, isLoading = false) }
    }

    is MemosAction.MemoDeleted -> {
      val updatedMemos = sortedMemos(state.memos.filterNot { it.name == action.name })
      state { copy(memos = updatedMemos, isLoading = false) }
    }

    is MemosAction.OperationFailed -> {
      state { copy(isLoading = false, errorMessage = action.error) }
    }
  }
}

private fun sortedMemos(memos: List<Memo>): List<Memo> =
  memos.sortedByDescending { memo ->
    memo.updateTime?.toEpochMilliseconds()
      ?: memo.createTime?.toEpochMilliseconds()
      ?: Long.MIN_VALUE
  }
