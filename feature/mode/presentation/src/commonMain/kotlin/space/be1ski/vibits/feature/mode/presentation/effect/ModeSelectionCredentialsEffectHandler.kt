package space.be1ski.vibits.feature.mode.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.core.logging.maskUrl
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.model.isFilled
import space.be1ski.vibits.feature.auth.domain.usecase.InitializeCredentialsFromEnvUseCase
import space.be1ski.vibits.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.feature.memos.domain.repository.ConnectionTester
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction

private const val TAG = "ModeCredentialsEffect"

class ModeSelectionCredentialsEffectHandler(
  private val connectionTester: ConnectionTester,
  private val initializeCredentialsFromEnv: InitializeCredentialsFromEnvUseCase,
  private val loadCredentials: LoadCredentialsUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
) : EffectHandler<ModeSelectionEffect.Command.Credentials, ModeSelectionAction> {
  override fun invoke(command: ModeSelectionEffect.Command.Credentials): Flow<ModeSelectionAction> =
    when (command) {
      ModeSelectionEffect.Command.InitializeFromLocalConfig -> handleInitializeFromLocalConfig()
      ModeSelectionEffect.Command.CheckStoredCredentials -> handleCheckStoredCredentials()
      ModeSelectionEffect.Command.UseStoredCredentialsWithValidation -> handleUseStoredCredentials()
      is ModeSelectionEffect.Command.ValidateCredentials -> handleValidateCredentials(command)
      is ModeSelectionEffect.Command.SaveCredentials -> handleSaveCredentials(command)
    }

  private fun handleInitializeFromLocalConfig(): Flow<ModeSelectionAction> =
    actions {
      initializeCredentialsFromEnv()
      // After initialization, check if credentials were loaded
      val credentials = loadCredentials()
      if (credentials.isFilled) {
        Log.d(TAG, "Stored credentials found after initialization")
        emit(ModeSelectionAction.StoredCredentialsFound)
      } else {
        Log.d(TAG, "No stored credentials after initialization")
        emit(ModeSelectionAction.StoredCredentialsNotFound)
      }
    }

  private fun handleCheckStoredCredentials(): Flow<ModeSelectionAction> =
    actions {
      val credentials = loadCredentials()
      if (credentials.isFilled) {
        Log.d(TAG, "Stored credentials found")
        emit(ModeSelectionAction.StoredCredentialsFound)
      } else {
        Log.d(TAG, "No stored credentials")
        emit(ModeSelectionAction.StoredCredentialsNotFound)
      }
    }

  private fun handleUseStoredCredentials(): Flow<ModeSelectionAction> =
    actions {
      // Ensure credentials are loaded from local config (in case of app reset)
      initializeCredentialsFromEnv()
      val credentials = loadCredentials()
      Log.d(TAG, "Using stored credentials: baseUrl=${credentials.baseUrl.maskUrl()}")
      connectionTester(credentials.baseUrl, credentials.token)
        .onSuccess { emit(ModeSelectionAction.ValidationSucceeded) }
        .onFailure { emit(ModeSelectionAction.ValidationFailed) }
    }

  private fun handleValidateCredentials(command: ModeSelectionEffect.Command.ValidateCredentials): Flow<ModeSelectionAction> =
    actions {
      Log.d(TAG, "Testing connection")
      connectionTester(command.baseUrl, command.token)
        .onSuccess { emit(ModeSelectionAction.ValidationSucceeded) }
        .onFailure { emit(ModeSelectionAction.ValidationFailed) }
    }

  private fun handleSaveCredentials(command: ModeSelectionEffect.Command.SaveCredentials): Flow<ModeSelectionAction> =
    actions {
      Log.d(TAG, "Saving credentials")
      saveCredentials(Credentials(command.baseUrl, command.token))
    }
}
