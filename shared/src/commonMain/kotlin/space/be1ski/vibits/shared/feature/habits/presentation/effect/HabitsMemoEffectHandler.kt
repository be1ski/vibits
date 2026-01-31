package space.be1ski.vibits.shared.feature.habits.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.habits.domain.usecase.SaveDailyHabitMemoUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.SaveDailyMemoResult
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

private const val TAG = "HabitsMemoEffect"

class HabitsMemoEffectHandler(
  private val memosRepository: MemosRepository,
  private val saveDailyHabitMemo: SaveDailyHabitMemoUseCase,
) : EffectHandler<HabitsEffect.Memo, HabitsAction> {
  override fun invoke(effect: HabitsEffect.Memo): Flow<HabitsAction> =
    when (effect) {
      is HabitsEffect.ToggleDailyHabit -> handleToggleDailyHabit(effect)
      is HabitsEffect.CreateMemo -> handleCreateMemo(effect)
      is HabitsEffect.UpdateMemo -> handleUpdateMemo(effect)
      is HabitsEffect.DeleteMemo -> handleDeleteMemo(effect)
    }

  private fun handleToggleDailyHabit(effect: HabitsEffect.ToggleDailyHabit): Flow<HabitsAction> =
    actions {
      Log.d(TAG, "Toggling habit ${effect.habitTag} for ${effect.date}")
      when (val result = saveDailyHabitMemo.toggleHabit(effect.date, effect.habitTag, effect.habitsConfig)) {
        is SaveDailyMemoResult.Created -> {
          Log.d(TAG, "Daily memo created: ${result.memo.name}")
          emit(HabitsAction.Response.MemoCreated(result.memo))
        }
        is SaveDailyMemoResult.Updated -> {
          Log.d(TAG, "Daily memo updated: ${result.memo.name}")
          emit(HabitsAction.Response.MemoUpdated(result.memo))
        }
        is SaveDailyMemoResult.Deleted -> {
          Log.d(TAG, "Daily memo deleted: ${result.memoName}")
          emit(HabitsAction.Response.MemoDeleted(result.memoName))
        }
        is SaveDailyMemoResult.Error -> {
          Log.e(TAG, "Failed to toggle habit: ${result.message}", result.exception)
          emit(HabitsAction.Response.MemoOperationFailed(result.message))
        }
      }
    }

  private fun handleCreateMemo(effect: HabitsEffect.CreateMemo): Flow<HabitsAction> =
    actions {
      Log.d(TAG, "Saving daily habit memo (atomic create-or-update)")
      when (val result = saveDailyHabitMemo(effect.content)) {
        is SaveDailyMemoResult.Created -> {
          Log.d(TAG, "Daily memo created: ${result.memo.name}")
          emit(HabitsAction.Response.MemoCreated(result.memo))
        }
        is SaveDailyMemoResult.Updated -> {
          Log.d(TAG, "Daily memo updated (race condition prevented): ${result.memo.name}")
          emit(HabitsAction.Response.MemoUpdated(result.memo))
        }
        is SaveDailyMemoResult.Deleted -> {
          // Shouldn't happen for CreateMemo, but handle for exhaustiveness
          Log.w(TAG, "Unexpected deletion result for CreateMemo")
          emit(HabitsAction.Response.MemoDeleted(result.memoName))
        }
        is SaveDailyMemoResult.Error -> {
          Log.e(TAG, "Failed to save daily habit memo: ${result.message}", result.exception)
          emit(HabitsAction.Response.MemoOperationFailed(result.message))
        }
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
