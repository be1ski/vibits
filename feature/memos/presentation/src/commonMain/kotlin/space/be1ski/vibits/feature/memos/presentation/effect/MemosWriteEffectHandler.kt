package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction

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
        .onSuccess { memo -> emit(MemosAction.Crud.MemoCreated(memo)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to create memo", error)
          emit(MemosAction.Crud.OperationFailed(error.message ?: "Failed to create memo"))
        }
    }

  private fun handleUpdateMemo(effect: MemosEffect.UpdateMemo): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Updating memo: ${effect.name}")
      runCatching { updateMemo(effect.name, effect.content) }
        .onSuccess { memo -> emit(MemosAction.Crud.MemoUpdated(memo)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to update memo", error)
          emit(MemosAction.Crud.OperationFailed(error.message ?: "Failed to update memo"))
        }
    }

  private fun handleDeleteMemo(effect: MemosEffect.DeleteMemo): Flow<MemosAction> =
    actions {
      Log.d(TAG, "Deleting memo: ${effect.name}")
      runCatching { deleteMemo(effect.name) }
        .onSuccess { emit(MemosAction.Crud.MemoDeleted(effect.name)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to delete memo", error)
          emit(MemosAction.Crud.OperationFailed(error.message ?: "Failed to delete memo"))
        }
    }
}
