package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

internal val loadingReducer: Reducer<MemosAction.Loading, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.Loading.LoadMemos -> {
        if (state.needsCredentials) {
          state { state.copy(credentialsMode = true, errorMessage = null) }
        } else {
          state { state.copy(isLoading = true, errorMessage = null, credentialsMode = false) }
          if (!state.isOfflineMode) {
            command(MemosEffect.SaveCredentials(state.baseUrl, state.token))
          }
          command(MemosEffect.LoadRemoteMemos)
        }
      }

      is MemosAction.Loading.LoadCachedMemos -> {
        command(MemosEffect.LoadCachedMemos)
      }

      is MemosAction.Loading.RefreshMemos -> {
        command(MemosEffect.RefreshMemos)
      }

      is MemosAction.Loading.ResetForModeChange -> {
        val isOffline = action.newMode == AppMode.OFFLINE || action.newMode == AppMode.DEMO
        state {
          state.copy(
            memos = emptyList(),
            memosRevision = state.memosRevision + 1,
            initialDataLoaded = false,
            isLoading = false,
            isOfflineMode = isOffline,
          )
        }
      }

      is MemosAction.Loading.ChangePostFilter -> {
        state { state.copy(activePostFilter = action.filter) }
      }

      is MemosAction.Loading.CachedMemosLoaded -> {
        if (state.memos.isNotEmpty()) {
          // Already have memos, don't overwrite with cache
          return@reducer
        }

        if (action.memos.isNotEmpty()) {
          // Cache has data, use it
          state {
            state.copy(
              memos = sortedMemos(action.memos),
              memosRevision = state.memosRevision + 1,
              initialDataLoaded = true,
            )
          }
        } else if (!state.isOfflineMode) {
          // Cache is empty and we're online - load from server immediately
          state { state.copy(isLoading = true) }
          command(MemosEffect.LoadRemoteMemos)
        } else {
          // Offline mode with no cache - mark as loaded
          state { state.copy(initialDataLoaded = true) }
        }
      }

      is MemosAction.Loading.MemosLoaded -> {
        state {
          state.copy(
            memos = sortedMemos(action.memos),
            memosRevision = state.memosRevision + 1,
            isLoading = false,
            errorMessage = null,
            initialDataLoaded = true,
          )
        }
      }
    }
  }
