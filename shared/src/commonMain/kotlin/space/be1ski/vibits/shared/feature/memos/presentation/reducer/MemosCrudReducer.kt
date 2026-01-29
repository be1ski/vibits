package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.ReducerResult
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.habits.domain.usecase.parseDailyDateFromContent
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState

private const val MILLIS_PER_DAY = 86400000L

internal fun crudReducer(
  action: MemosAction.Crud,
  state: MemosState,
): ReducerResult<MemosState, MemosEffect, Nothing> =
  reducer<MemosAction.Crud, MemosState, MemosEffect, Nothing> { a, s ->
    when (a) {
      is MemosAction.Crud.CreateMemo -> {
        state { copy(isLoading = true) }
        command(MemosEffect.CreateMemo(a.content))
      }

      is MemosAction.Crud.UpdateMemo -> {
        state { copy(isLoading = true) }
        command(MemosEffect.UpdateMemo(a.name, a.content))
      }

      is MemosAction.Crud.DeleteMemo -> {
        state { copy(isLoading = true) }
        command(MemosEffect.DeleteMemo(a.name))
      }

      is MemosAction.Crud.MemoCreated -> {
        val updatedMemos = sortedMemos(s.memos + a.memo)
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
      }

      is MemosAction.Crud.MemoUpdated -> {
        val updatedMemos =
          sortedMemos(
            s.memos.map { memo ->
              if (memo.name == a.memo.name) a.memo else memo
            },
          )
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
      }

      is MemosAction.Crud.MemoDeleted -> {
        val updatedMemos = sortedMemos(s.memos.filterNot { it.name == a.name })
        state { copy(memos = updatedMemos, memosRevision = memosRevision + 1, isLoading = false) }
      }

      is MemosAction.Crud.OperationFailed -> {
        state { copy(isLoading = false, errorMessage = a.error) }
      }
    }
  }(action, state)

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
