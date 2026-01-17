package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

private const val TAG = "HabitsEffect"

/**
 * Effect handler for the Habits feature.
 * Converts side effects into actions.
 */
class HabitsEffectHandler(
  private val memosRepository: MemosRepository,
  private val onRefresh: () -> Unit,
) : EffectHandler<HabitsEffect, HabitsAction> {
  override fun invoke(effect: HabitsEffect): Flow<HabitsAction> =
    flow {
      when (effect) {
        is HabitsEffect.CreateMemo -> {
          Log.d(TAG, "Creating habit memo")
          runCatching { memosRepository.createMemo(effect.content) }
            .onSuccess { memo -> emit(HabitsAction.MemoCreated(memo)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to create habit memo", error)
              emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to create memo"))
            }
        }

        is HabitsEffect.UpdateMemo -> {
          Log.d(TAG, "Updating habit memo: ${effect.name}")
          runCatching { memosRepository.updateMemo(effect.name, effect.content) }
            .onSuccess { memo -> emit(HabitsAction.MemoUpdated(memo)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to update habit memo", error)
              emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to update memo"))
            }
        }

        is HabitsEffect.DeleteMemo -> {
          Log.d(TAG, "Deleting habit memo: ${effect.name}")
          runCatching { memosRepository.deleteMemo(effect.name) }
            .onSuccess { emit(HabitsAction.MemoDeleted(effect.name)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to delete habit memo", error)
              emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to delete memo"))
            }
        }

        is HabitsEffect.RefreshMemos -> {
          Log.d(TAG, "Refreshing memos")
          onRefresh()
        }
      }
    }
}
