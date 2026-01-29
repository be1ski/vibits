package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState

@Suppress("LongMethod")
internal fun loadingReducer(
  action: MemosAction.Loading,
  state: MemosState,
): ReducerResult<MemosState, MemosEffect, Nothing> =
  reducer<MemosAction.Loading, MemosState, MemosEffect, Nothing> { a, s ->
    when (a) {
      is MemosAction.Loading.LoadMemos -> {
        if (s.needsCredentials) {
          state { copy(credentialsMode = true, errorMessage = "Base URL and token are required.") }
        } else {
          state { copy(isLoading = true, errorMessage = null, credentialsMode = false) }
          if (!s.isOfflineMode) {
            command(MemosEffect.SaveCredentials(s.baseUrl, s.token))
          }
          command(MemosEffect.LoadRemoteMemos)
        }
      }

      is MemosAction.Loading.LoadCachedMemos -> {
        command(MemosEffect.LoadCachedMemos)
      }

      is MemosAction.Loading.ResetForModeChange -> {
        state {
          copy(
            memos = emptyList(),
            memosRevision = memosRevision + 1,
            initialDataLoaded = false,
            isLoading = false,
          )
        }
      }

      is MemosAction.Loading.ChangePostFilter -> {
        state { copy(activePostFilter = a.filter) }
      }

      is MemosAction.Loading.CachedMemosLoaded -> {
        if (s.memos.isNotEmpty()) {
          // Already have memos, don't overwrite with cache
          return@reducer
        }

        if (a.memos.isNotEmpty()) {
          // Cache has data, use it
          state {
            copy(
              memos = sortedMemos(a.memos),
              memosRevision = memosRevision + 1,
              initialDataLoaded = true,
            )
          }
        } else if (!s.isOfflineMode) {
          // Cache is empty and we're online - load from server immediately
          state { copy(isLoading = true) }
          command(MemosEffect.LoadRemoteMemos)
        } else {
          // Offline mode with no cache - mark as loaded
          state { copy(initialDataLoaded = true) }
        }
      }

      is MemosAction.Loading.MemosLoaded -> {
        state {
          copy(
            memos = sortedMemos(a.memos),
            memosRevision = memosRevision + 1,
            isLoading = false,
            errorMessage = null,
            initialDataLoaded = true,
          )
        }
      }
    }
  }(action, state)

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

private const val MILLIS_PER_DAY = 86400000L
