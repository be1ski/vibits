package space.be1ski.memos.shared.presentation.memos

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.be1ski.memos.shared.feature.auth.domain.model.Credentials
import space.be1ski.memos.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.memos.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.memos.shared.core.elm.EffectHandler

/**
 * Effect handler for the Memos feature.
 */
class MemosEffectHandler(
  private val loadMemosUseCase: LoadMemosUseCase,
  private val loadCachedMemosUseCase: LoadCachedMemosUseCase,
  private val loadCredentialsUseCase: LoadCredentialsUseCase,
  private val saveCredentialsUseCase: SaveCredentialsUseCase,
  private val createMemoUseCase: CreateMemoUseCase,
  private val updateMemoUseCase: UpdateMemoUseCase,
  private val deleteMemoUseCase: DeleteMemoUseCase
) : EffectHandler<MemosEffect, MemosAction> {

  override fun invoke(effect: MemosEffect): Flow<MemosAction> = flow {
    when (effect) {
      is MemosEffect.LoadCredentials -> {
        val creds = loadCredentialsUseCase()
        emit(MemosAction.CredentialsLoaded(creds.baseUrl, creds.token))
      }

      is MemosEffect.SaveCredentials -> {
        saveCredentialsUseCase(Credentials(baseUrl = effect.baseUrl, token = effect.token))
      }

      is MemosEffect.LoadCachedMemos -> {
        runCatching { loadCachedMemosUseCase() }
          .onSuccess { memos -> emit(MemosAction.CachedMemosLoaded(memos)) }
      }

      is MemosEffect.LoadRemoteMemos -> {
        runCatching { loadMemosUseCase() }
          .onSuccess { memos -> emit(MemosAction.MemosLoaded(memos)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to load memos"))
          }
      }

      is MemosEffect.CreateMemo -> {
        runCatching { createMemoUseCase(effect.content) }
          .onSuccess { memo -> emit(MemosAction.MemoCreated(memo)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to create memo"))
          }
      }

      is MemosEffect.UpdateMemo -> {
        runCatching { updateMemoUseCase(effect.name, effect.content) }
          .onSuccess { memo -> emit(MemosAction.MemoUpdated(memo)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to update memo"))
          }
      }

      is MemosEffect.DeleteMemo -> {
        runCatching { deleteMemoUseCase(effect.name) }
          .onSuccess { emit(MemosAction.MemoDeleted(effect.name)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to delete memo"))
          }
      }
    }
  }
}
