package space.be1ski.vibits.shared.feature.memos.presentation

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.UpdateMemoUseCase

private const val TAG = "MemosWriteEffect"

class MemosWriteEffectHandler(
  private val createMemo: CreateMemoUseCase,
  private val updateMemo: UpdateMemoUseCase,
  private val deleteMemo: DeleteMemoUseCase,
) : EffectHandler<MemosEffect.Write, MemosAction> {
  override fun invoke(effect: MemosEffect.Write): Flow<MemosAction> =
    when (effect) {
      is MemosEffect.CreateMemo -> handleCreateMemo(effect)
      is MemosEffect.UpdateMemo -> handleUpdateMemo(effect)
      is MemosEffect.DeleteMemo -> handleDeleteMemo(effect)
    }

  private fun handleCreateMemo(effect: MemosEffect.CreateMemo): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Creating memo")
      runCatching { createMemo(effect.content) }
        .onSuccess { memo -> emit(MemosAction.MemoCreated(memo)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to create memo", error)
          emit(MemosAction.OperationFailed(error.message ?: "Failed to create memo"))
        }
    }

  private fun handleUpdateMemo(effect: MemosEffect.UpdateMemo): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Updating memo: ${effect.name}")
      runCatching { updateMemo(effect.name, effect.content) }
        .onSuccess { memo -> emit(MemosAction.MemoUpdated(memo)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to update memo", error)
          emit(MemosAction.OperationFailed(error.message ?: "Failed to update memo"))
        }
    }

  private fun handleDeleteMemo(effect: MemosEffect.DeleteMemo): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Deleting memo: ${effect.name}")
      runCatching { deleteMemo(effect.name) }
        .onSuccess { emit(MemosAction.MemoDeleted(effect.name)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to delete memo", error)
          emit(MemosAction.OperationFailed(error.message ?: "Failed to delete memo"))
        }
    }
}
