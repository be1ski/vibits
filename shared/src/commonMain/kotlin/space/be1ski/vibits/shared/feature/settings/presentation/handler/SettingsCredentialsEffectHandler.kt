package space.be1ski.vibits.shared.feature.settings.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.data.ConnectionTester
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect

private const val TAG = "SettingsCredentialsEffect"

class SettingsCredentialsEffectHandler(
  private val connectionTester: ConnectionTester,
  private val saveCredentials: SaveCredentialsUseCase,
) : EffectHandler<SettingsEffect.Command.Credentials, SettingsAction> {
  override fun invoke(command: SettingsEffect.Command.Credentials): Flow<SettingsAction> =
    when (command) {
      is SettingsEffect.Command.ValidateCredentials -> handleValidateCredentials(command)
      is SettingsEffect.Command.SaveCredentials -> handleSaveCredentials(command)
    }

  private fun handleValidateCredentials(command: SettingsEffect.Command.ValidateCredentials): Flow<SettingsAction> =
    actions {
      Log.d(TAG, "Testing connection")
      connectionTester(command.baseUrl, command.token)
        .onSuccess { emit(SettingsAction.Validation.ValidationSucceeded) }
        .onFailure { emit(SettingsAction.Validation.ValidationFailed("connection_failed")) }
    }

  private fun handleSaveCredentials(command: SettingsEffect.Command.SaveCredentials): Flow<SettingsAction> =
    actions {
      Log.d(TAG, "Saving credentials")
      saveCredentials(Credentials(command.baseUrl, command.token))
    }
}
