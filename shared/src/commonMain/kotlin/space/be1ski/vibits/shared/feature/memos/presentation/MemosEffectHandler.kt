package space.be1ski.vibits.shared.feature.memos.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.UpdateMemoUseCase

private const val TAG = "MemosEffect"

@Suppress("LongParameterList")
class MemosEffectHandler(
  private val loadCredentials: LoadCredentialsUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
  private val loadMemos: LoadMemosUseCase,
  private val loadCachedMemos: LoadCachedMemosUseCase,
  private val createMemo: CreateMemoUseCase,
  private val updateMemo: UpdateMemoUseCase,
  private val deleteMemo: DeleteMemoUseCase,
) : EffectHandler<MemosEffect, MemosAction> {
  override fun invoke(effect: MemosEffect): Flow<MemosAction> =
    flow {
      when (effect) {
        is MemosEffect.LoadCredentials -> {
          val creds = loadCredentials()
          emit(MemosAction.CredentialsLoaded(creds.baseUrl, creds.token))
        }

        is MemosEffect.SaveCredentials -> {
          saveCredentials(Credentials(baseUrl = effect.baseUrl, token = effect.token))
        }

        is MemosEffect.LoadCachedMemos -> {
          Log.d(TAG, "Loading cached memos")
          runCatching { loadCachedMemos() }
            .onSuccess { memos -> emit(MemosAction.CachedMemosLoaded(memos)) }
        }

        is MemosEffect.LoadRemoteMemos -> {
          Log.d(TAG, "Loading memos")
          runCatching { loadMemos() }
            .onSuccess { memos -> emit(MemosAction.MemosLoaded(memos)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to load memos", error)
              emit(MemosAction.OperationFailed(error.message ?: "Failed to load memos"))
            }
        }

        is MemosEffect.CreateMemo -> {
          Log.d(TAG, "Creating memo")
          runCatching { createMemo(effect.content) }
            .onSuccess { memo -> emit(MemosAction.MemoCreated(memo)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to create memo", error)
              emit(MemosAction.OperationFailed(error.message ?: "Failed to create memo"))
            }
        }

        is MemosEffect.UpdateMemo -> {
          Log.d(TAG, "Updating memo: ${effect.name}")
          runCatching { updateMemo(effect.name, effect.content) }
            .onSuccess { memo -> emit(MemosAction.MemoUpdated(memo)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to update memo", error)
              emit(MemosAction.OperationFailed(error.message ?: "Failed to update memo"))
            }
        }

        is MemosEffect.DeleteMemo -> {
          Log.d(TAG, "Deleting memo: ${effect.name}")
          runCatching { deleteMemo(effect.name) }
            .onSuccess { emit(MemosAction.MemoDeleted(effect.name)) }
            .onFailure { error ->
              Log.e(TAG, "Failed to delete memo", error)
              emit(MemosAction.OperationFailed(error.message ?: "Failed to delete memo"))
            }
        }
      }
    }
}
