package space.be1ski.vibits.shared.feature.memos.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect

private const val TAG = "MemosLoadEffect"

class MemosLoadEffectHandler(
  private val loadMemos: LoadMemosUseCase,
  private val loadCachedMemos: LoadCachedMemosUseCase,
) : EffectHandler<MemosEffect.Load, MemosAction> {
  override fun invoke(effect: MemosEffect.Load): Flow<MemosAction> =
    when (effect) {
      MemosEffect.LoadCachedMemos -> handleLoadCachedMemos()
      MemosEffect.LoadRemoteMemos -> handleLoadRemoteMemos()
    }

  private fun handleLoadCachedMemos(): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Loading cached memos")
      runCatching { loadCachedMemos() }
        .onSuccess { memos -> emit(MemosAction.CachedMemosLoaded(memos)) }
    }

  private fun handleLoadRemoteMemos(): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Loading memos")
      runCatching { loadMemos() }
        .onSuccess { memos -> emit(MemosAction.MemosLoaded(memos)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to load memos", error)
          emit(MemosAction.OperationFailed(error.message ?: "Failed to load memos"))
        }
    }
}
