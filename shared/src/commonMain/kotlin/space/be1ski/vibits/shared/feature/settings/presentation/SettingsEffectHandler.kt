package space.be1ski.vibits.shared.feature.settings.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.data.ConnectionTester
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.ResetAppWithMemosUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveThemeUseCase

private const val TAG = "SettingsEffect"

@Suppress("LongParameterList")
class SettingsEffectHandler(
  private val connectionTester: ConnectionTester,
  private val switchAppMode: SwitchAppModeUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
  private val resetApp: ResetAppUseCase,
  private val resetAppWithMemos: ResetAppWithMemosUseCase,
  private val saveLanguage: SaveLanguageUseCase,
  private val saveTheme: SaveThemeUseCase,
) : EffectHandler<SettingsEffect.Command, SettingsAction> {
  override fun invoke(command: SettingsEffect.Command): Flow<SettingsAction> =
    when (command) {
      is SettingsEffect.Command.ValidateCredentials -> handleValidateCredentials(command)
      is SettingsEffect.Command.SwitchMode -> handleSwitchMode(command)
      is SettingsEffect.Command.SaveCredentials -> handleSaveCredentials(command)
      is SettingsEffect.Command.ResetApp -> handleResetApp()
      is SettingsEffect.Command.ResetAppWithMemos -> handleResetAppWithMemos()
      is SettingsEffect.Command.SaveLanguage -> handleSaveLanguage(command)
      is SettingsEffect.Command.SaveTheme -> handleSaveTheme(command)
    }

  private fun handleValidateCredentials(command: SettingsEffect.Command.ValidateCredentials): Flow<SettingsAction> =
    flow {
      Log.d(TAG, "Testing connection")
      connectionTester(command.baseUrl, command.token)
        .onSuccess { emit(SettingsAction.ValidationSucceeded) }
        .onFailure { emit(SettingsAction.ValidationFailed("connection_failed")) }
    }

  private fun handleSwitchMode(command: SettingsEffect.Command.SwitchMode): Flow<SettingsAction> =
    flow {
      Log.i(TAG, "Switching mode to ${command.mode}")
      switchAppMode(command.mode)
      emit(SettingsAction.ModeSwitched)
    }

  private fun handleSaveCredentials(command: SettingsEffect.Command.SaveCredentials): Flow<SettingsAction> =
    flow {
      Log.d(TAG, "Saving credentials")
      saveCredentials(Credentials(command.baseUrl, command.token))
    }

  private fun handleResetApp(): Flow<SettingsAction> =
    flow {
      Log.i(TAG, "Resetting app")
      resetApp()
      emit(SettingsAction.ResetCompleted)
    }

  private fun handleResetAppWithMemos(): Flow<SettingsAction> =
    flow {
      Log.i(TAG, "Resetting app with memos")
      resetAppWithMemos()
      emit(SettingsAction.ResetCompleted)
    }

  private fun handleSaveLanguage(command: SettingsEffect.Command.SaveLanguage): Flow<SettingsAction> =
    emptyFlow<SettingsAction>().also {
      Log.i(TAG, "Language changed to ${command.language}")
      saveLanguage(command.language)
    }

  private fun handleSaveTheme(command: SettingsEffect.Command.SaveTheme): Flow<SettingsAction> =
    emptyFlow<SettingsAction>().also {
      Log.i(TAG, "Theme changed to ${command.theme}")
      saveTheme(command.theme)
    }
}
