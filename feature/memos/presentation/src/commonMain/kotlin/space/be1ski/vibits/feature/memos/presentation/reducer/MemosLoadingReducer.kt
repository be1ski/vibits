package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

@Suppress("LongMethod")
internal val loadingReducer: Reducer<MemosAction.Loading, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.Loading.LoadMemos -> {
        if (state.needsCredentials) {
          state { state.copy(credentialsMode = true, errorMessage = "Base URL and token are required.") }
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

private fun sortedMemos(memos: List<Memo>): List<Memo> {
  return memos.sortedByDescending { memo ->
    // For habit tracking posts, use the tracked date instead of creation date
    val trackingDate =
      space.be1ski.vibits.feature.habits.domain.usecase
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

private const val MILLIS_PER_DAY = 86400000L
