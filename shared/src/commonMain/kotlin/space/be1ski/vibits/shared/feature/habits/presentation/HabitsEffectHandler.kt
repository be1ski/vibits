package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.core.elm.EffectHandler

/**
 * Effect handler for the Habits feature.
 * Converts side effects into actions.
 */
class HabitsEffectHandler(
  private val memosRepository: MemosRepository,
  private val onRefresh: () -> Unit
) : EffectHandler<HabitsEffect, HabitsAction> {

  override fun invoke(effect: HabitsEffect): Flow<HabitsAction> = flow {
    when (effect) {
      is HabitsEffect.CreateMemo -> {
        runCatching { memosRepository.createMemo(effect.content) }
          .onSuccess { memo -> emit(HabitsAction.MemoCreated(memo)) }
          .onFailure { error ->
            emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to create memo"))
          }
      }

      is HabitsEffect.UpdateMemo -> {
        runCatching { memosRepository.updateMemo(effect.name, effect.content) }
          .onSuccess { memo -> emit(HabitsAction.MemoUpdated(memo)) }
          .onFailure { error ->
            emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to update memo"))
          }
      }

      is HabitsEffect.DeleteMemo -> {
        runCatching { memosRepository.deleteMemo(effect.name) }
          .onSuccess { emit(HabitsAction.MemoDeleted(effect.name)) }
          .onFailure { error ->
            emit(HabitsAction.MemoOperationFailed(error.message ?: "Failed to delete memo"))
          }
      }

      is HabitsEffect.RefreshMemos -> {
        onRefresh()
      }
    }
  }
}
