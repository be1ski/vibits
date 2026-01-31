package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.domain.usecase.parseDailyDateFromContent
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

private const val MILLIS_PER_DAY = 86400000L

internal val crudReducer: Reducer<MemosAction.Crud, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    when (action) {
      is MemosAction.Crud.CreateMemo -> {
        state { copy(isLoading = true) }
        command(MemosEffect.CreateMemo(action.content))
      }

      is MemosAction.Crud.UpdateMemo -> {
        state { copy(isLoading = true) }
        command(MemosEffect.UpdateMemo(action.name, action.content))
      }

      is MemosAction.Crud.DeleteMemo -> {
        state { copy(isLoading = true) }
        command(MemosEffect.DeleteMemo(action.name))
      }

      is MemosAction.Crud.MemoCreated -> {
        val updatedMemos = sortedMemos(state.memos + action.memo)
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
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
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
        // Trigger sync to push local changes to server in online mode
        if (!state.isOfflineMode) {
          command(MemosEffect.PerformSync)
        }
      }

      is MemosAction.Crud.MemoDeleted -> {
        val updatedMemos = sortedMemos(state.memos.filterNot { it.name == action.name })
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
        // Trigger sync to push local changes to server in online mode
        if (!state.isOfflineMode) {
          command(MemosEffect.PerformSync)
        }
      }

      is MemosAction.Crud.OperationFailed -> {
        state { copy(isLoading = false, errorMessage = action.error) }
      }
    }
  }

private fun sortedMemos(memos: List<Memo>): List<Memo> {
  return memos.sortedByDescending { memo ->
    // For habit tracking posts, use the tracked date instead of creation date
    val trackingDate = parseDailyDateFromContent(memo.content)
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
