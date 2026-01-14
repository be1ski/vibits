package space.be1ski.memos.shared.feature.memos.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.be1ski.memos.shared.feature.auth.domain.model.Credentials
import space.be1ski.memos.shared.core.elm.EffectHandler

class MemosEffectHandler(
  private val useCases: MemosUseCases
) : EffectHandler<MemosEffect, MemosAction> {

  override fun invoke(effect: MemosEffect): Flow<MemosAction> = flow {
    when (effect) {
      is MemosEffect.LoadCredentials -> {
        val creds = useCases.loadCredentials()
        emit(MemosAction.CredentialsLoaded(creds.baseUrl, creds.token))
      }

      is MemosEffect.SaveCredentials -> {
        useCases.saveCredentials(Credentials(baseUrl = effect.baseUrl, token = effect.token))
      }

      is MemosEffect.LoadCachedMemos -> {
        runCatching { useCases.loadCachedMemos() }
          .onSuccess { memos -> emit(MemosAction.CachedMemosLoaded(memos)) }
      }

      is MemosEffect.LoadRemoteMemos -> {
        runCatching { useCases.loadMemos() }
          .onSuccess { memos -> emit(MemosAction.MemosLoaded(memos)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to load memos"))
          }
      }

      is MemosEffect.CreateMemo -> {
        runCatching { useCases.createMemo(effect.content) }
          .onSuccess { memo -> emit(MemosAction.MemoCreated(memo)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to create memo"))
          }
      }

      is MemosEffect.UpdateMemo -> {
        runCatching { useCases.updateMemo(effect.name, effect.content) }
          .onSuccess { memo -> emit(MemosAction.MemoUpdated(memo)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to update memo"))
          }
      }

      is MemosEffect.DeleteMemo -> {
        runCatching { useCases.deleteMemo(effect.name) }
          .onSuccess { emit(MemosAction.MemoDeleted(effect.name)) }
          .onFailure { error ->
            emit(MemosAction.OperationFailed(error.message ?: "Failed to delete memo"))
          }
      }
    }
  }
}
