package space.be1ski.vibits.shared.feature.habits.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

private const val TAG = "HabitsMemoEffect"

class HabitsMemoEffectHandler(
  private val memosRepository: MemosRepository,
) : EffectHandler<HabitsEffect.Memo, HabitsAction> {
  override fun invoke(effect: HabitsEffect.Memo): Flow<HabitsAction> =
    when (effect) {
      is HabitsEffect.CreateMemo -> handleCreateMemo(effect)
      is HabitsEffect.UpdateMemo -> handleUpdateMemo(effect)
      is HabitsEffect.DeleteMemo -> handleDeleteMemo(effect)
    }

  private fun handleCreateMemo(effect: HabitsEffect.CreateMemo): Flow<HabitsAction> =
    actions {
      Log.d(TAG, "Creating habit memo")
      runCatching { memosRepository.createMemo(effect.content) }
        .onSuccess { memo -> emit(HabitsAction.Response.MemoCreated(memo)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to create habit memo", error)
          emit(HabitsAction.Response.MemoOperationFailed(error.message ?: "Failed to create memo"))
        }
    }

  private fun handleUpdateMemo(effect: HabitsEffect.UpdateMemo): Flow<HabitsAction> =
    actions {
      Log.d(TAG, "Updating habit memo: ${effect.name}")
      runCatching { memosRepository.updateMemo(effect.name, effect.content) }
        .onSuccess { memo -> emit(HabitsAction.Response.MemoUpdated(memo)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to update habit memo", error)
          emit(HabitsAction.Response.MemoOperationFailed(error.message ?: "Failed to update memo"))
        }
    }

  private fun handleDeleteMemo(effect: HabitsEffect.DeleteMemo): Flow<HabitsAction> =
    actions {
      Log.d(TAG, "Deleting habit memo: ${effect.name}")
      runCatching { memosRepository.deleteMemo(effect.name) }
        .onSuccess { emit(HabitsAction.Response.MemoDeleted(effect.name)) }
        .onFailure { error ->
          Log.e(TAG, "Failed to delete habit memo", error)
          emit(HabitsAction.Response.MemoOperationFailed(error.message ?: "Failed to delete memo"))
        }
    }
}
