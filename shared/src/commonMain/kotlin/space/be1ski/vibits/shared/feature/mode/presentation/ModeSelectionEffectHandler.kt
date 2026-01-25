package space.be1ski.vibits.shared.feature.mode.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.data.ConnectionTester
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase

private const val TAG = "ModeEffect"

class ModeSelectionEffectHandler(
  private val connectionTester: ConnectionTester,
  private val initializeCredentialsFromEnv: space.be1ski.vibits.shared.feature.auth.domain.usecase.InitializeCredentialsFromEnvUseCase,
  private val loadCredentials: LoadCredentialsUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
  private val saveAppMode: SaveAppModeUseCase,
) : EffectHandler<ModeSelectionEffect, ModeSelectionAction> {
  override fun invoke(effect: ModeSelectionEffect): Flow<ModeSelectionAction> =
    when (effect) {
      is ModeSelectionEffect.InitializeFromLocalConfig -> handleInitializeFromLocalConfig()
      is ModeSelectionEffect.CheckStoredCredentials -> handleCheckStoredCredentials()
      is ModeSelectionEffect.UseStoredCredentialsWithValidation -> handleUseStoredCredentials()
      is ModeSelectionEffect.ValidateCredentials -> handleValidateCredentials(effect)
      is ModeSelectionEffect.SaveCredentials -> handleSaveCredentials(effect)
      is ModeSelectionEffect.SaveMode -> handleSaveMode(effect)
      // Parent notification effects are not handled here - they flow through to AppRoot
      is ModeSelectionEffect.NotifyModeSelected -> emptyFlow()
    }

  private fun handleInitializeFromLocalConfig(): Flow<ModeSelectionAction> =
    flow {
      initializeCredentialsFromEnv()
      // After initialization, check if credentials were loaded
      val credentials = loadCredentials()
      if (credentials.baseUrl.isNotBlank() && credentials.token.isNotBlank()) {
        Log.d(TAG, "Stored credentials found after initialization")
        emit(ModeSelectionAction.StoredCredentialsFound)
      } else {
        Log.d(TAG, "No stored credentials after initialization")
        emit(ModeSelectionAction.StoredCredentialsNotFound)
      }
    }

  private fun handleCheckStoredCredentials(): Flow<ModeSelectionAction> =
    flow {
      val credentials = loadCredentials()
      if (credentials.baseUrl.isNotBlank() && credentials.token.isNotBlank()) {
        Log.d(TAG, "Stored credentials found")
        emit(ModeSelectionAction.StoredCredentialsFound)
      } else {
        Log.d(TAG, "No stored credentials")
        emit(ModeSelectionAction.StoredCredentialsNotFound)
      }
    }

  private fun handleUseStoredCredentials(): Flow<ModeSelectionAction> =
    flow {
      val credentials = loadCredentials()
      Log.d(TAG, "Using stored credentials")
      connectionTester(credentials.baseUrl, credentials.token)
        .onSuccess { emit(ModeSelectionAction.ValidationSucceeded) }
        .onFailure { emit(ModeSelectionAction.ValidationFailed) }
    }

  private fun handleValidateCredentials(effect: ModeSelectionEffect.ValidateCredentials): Flow<ModeSelectionAction> =
    flow {
      Log.d(TAG, "Testing connection")
      connectionTester(effect.baseUrl, effect.token)
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
