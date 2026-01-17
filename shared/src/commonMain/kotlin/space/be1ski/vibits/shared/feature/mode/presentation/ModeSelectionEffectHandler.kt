package space.be1ski.vibits.shared.feature.mode.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase

private const val TAG = "ModeEffect"

class ModeSelectionEffectHandler(
  private val validateCredentials: ValidateCredentialsUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
  private val saveAppMode: SaveAppModeUseCase,
) : EffectHandler<ModeSelectionEffect, ModeSelectionAction> {
  override fun invoke(effect: ModeSelectionEffect): Flow<ModeSelectionAction> =
    when (effect) {
      is ModeSelectionEffect.ValidateCredentials -> handleValidateCredentials(effect)
      is ModeSelectionEffect.SaveCredentials -> handleSaveCredentials(effect)
      is ModeSelectionEffect.SaveMode -> handleSaveMode(effect)
      // Parent notification effects are not handled here - they flow through to AppRoot
      is ModeSelectionEffect.NotifyModeSelected -> emptyFlow()
    }

  private fun handleValidateCredentials(effect: ModeSelectionEffect.ValidateCredentials): Flow<ModeSelectionAction> =
    flow {
      Log.d(TAG, "Validating credentials")
      validateCredentials(effect.baseUrl, effect.token)
        .onSuccess { emit(ModeSelectionAction.ValidationSucceeded) }
        .onFailure { emit(ModeSelectionAction.ValidationFailed) }
    }

  private fun handleSaveCredentials(effect: ModeSelectionEffect.SaveCredentials): Flow<ModeSelectionAction> =
    flow {
      Log.d(TAG, "Saving credentials")
      saveCredentials(Credentials(effect.baseUrl, effect.token))
    }

  private fun handleSaveMode(effect: ModeSelectionEffect.SaveMode): Flow<ModeSelectionAction> =
    flow {
      Log.i(TAG, "Saving mode: ${effect.mode}")
      saveAppMode(effect.mode)
    }
}
