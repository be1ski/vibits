package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction

class MemosCredentialsEffectHandler(
  private val loadCredentials: LoadCredentialsUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
) : EffectHandler<MemosEffect.Credentials, MemosAction> {
  override fun invoke(effect: MemosEffect.Credentials): Flow<MemosAction> =
    when (effect) {
      MemosEffect.LoadCredentials -> handleLoadCredentials()
      is MemosEffect.SaveCredentials -> handleSaveCredentials(effect)
    }

  private fun handleLoadCredentials(): Flow<MemosAction> =
    actions {
      val creds = loadCredentials()
      emit(MemosAction.Credentials.CredentialsLoaded(creds.baseUrl, creds.token))
    }

  private fun handleSaveCredentials(effect: MemosEffect.SaveCredentials): Flow<MemosAction> =
    actions {
      saveCredentials(Credentials(baseUrl = effect.baseUrl, token = effect.token))
    }
}
