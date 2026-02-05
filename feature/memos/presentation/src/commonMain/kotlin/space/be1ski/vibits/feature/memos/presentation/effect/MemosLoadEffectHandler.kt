package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction

private const val TAG = "MemosLoadEffect"

class MemosLoadEffectHandler(
  private val loadMemos: LoadMemosUseCase,
  private val loadCachedMemos: LoadCachedMemosUseCase,
) : EffectHandler<MemosEffect.Load, MemosAction> {
  override fun invoke(effect: MemosEffect.Load): Flow<MemosAction> =
    when (effect) {
      MemosEffect.LoadCachedMemos -> handleLoadCachedMemos()
      MemosEffect.LoadRemoteMemos -> handleLoadRemoteMemos()
      MemosEffect.RefreshMemos -> handleRefreshMemos()
    }

  private fun handleLoadCachedMemos(): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Loading cached memos")
      runCatching { loadCachedMemos() }
        .onSuccess { memos -> emit(MemosAction.Loading.CachedMemosLoaded(memos)) }
    }

  private fun handleRefreshMemos(): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Refreshing memos from cache")
      runCatching { loadCachedMemos() }
        .onSuccess { memos ->
          Log.d(TAG, "Refreshed ${memos.size} memos")
          emit(MemosAction.Loading.MemosLoaded(memos))
        }
    }

  private fun handleLoadRemoteMemos(): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Loading memos")
      runCatching { loadMemos() }
        .onSuccess { memos -> emit(MemosAction.Loading.MemosLoaded(memos)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to load memos", error)
          emit(MemosAction.Crud.OperationFailed(error.message ?: "Failed to load memos"))
        }
    }
}
