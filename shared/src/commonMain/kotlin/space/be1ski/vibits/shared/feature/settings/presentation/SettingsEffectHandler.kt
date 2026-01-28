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
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveThemeUseCase

private const val TAG = "SettingsEffect"

class SettingsEffectHandler(
  private val connectionTester: ConnectionTester,
  private val switchAppMode: SwitchAppModeUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
  private val resetApp: ResetAppUseCase,
  private val saveLanguage: SaveLanguageUseCase,
  private val saveTheme: SaveThemeUseCase,
) : EffectHandler<SettingsEffect, SettingsAction> {
  override fun invoke(effect: SettingsEffect): Flow<SettingsAction> =
    when (effect) {
      is SettingsEffect.Command -> handleCommand(effect)
      is SettingsEffect.Notification -> emptyFlow()
    }

  private fun handleCommand(effect: SettingsEffect.Command): Flow<SettingsAction> =
    when (effect) {
      is SettingsEffect.Command.ValidateCredentials -> handleValidateCredentials(effect)
      is SettingsEffect.Command.SwitchMode -> handleSwitchMode(effect)
      is SettingsEffect.Command.SaveCredentials -> handleSaveCredentials(effect)
      is SettingsEffect.Command.ResetApp -> handleResetApp()
      is SettingsEffect.Command.SaveLanguage -> handleSaveLanguage(effect)
      is SettingsEffect.Command.SaveTheme -> handleSaveTheme(effect)
    }

  private fun handleValidateCredentials(effect: SettingsEffect.Command.ValidateCredentials): Flow<SettingsAction> =
    flow {
      Log.d(TAG, "Testing connection")
      connectionTester(effect.baseUrl, effect.token)
        .onSuccess { emit(SettingsAction.ValidationSucceeded) }
        .onFailure { emit(SettingsAction.ValidationFailed("connection_failed")) }
    }

  private fun handleSwitchMode(effect: SettingsEffect.Command.SwitchMode): Flow<SettingsAction> =
    flow {
      Log.i(TAG, "Switching mode to ${effect.mode}")
      switchAppMode(effect.mode)
      emit(SettingsAction.ModeSwitched)
    }

  private fun handleSaveCredentials(effect: SettingsEffect.Command.SaveCredentials): Flow<SettingsAction> =
    flow {
      Log.d(TAG, "Saving credentials")
      saveCredentials(Credentials(effect.baseUrl, effect.token))
    }

  private fun handleResetApp(): Flow<SettingsAction> =
    flow {
      Log.i(TAG, "Resetting app")
      resetApp()
      emit(SettingsAction.ResetCompleted)
    }

  private fun handleSaveLanguage(effect: SettingsEffect.Command.SaveLanguage): Flow<SettingsAction> =
    emptyFlow<SettingsAction>().also {
      Log.i(TAG, "Language changed to ${effect.language}")
      saveLanguage(effect.language)
    }

  private fun handleSaveTheme(effect: SettingsEffect.Command.SaveTheme): Flow<SettingsAction> =
    emptyFlow<SettingsAction>().also {
      Log.i(TAG, "Theme changed to ${effect.theme}")
      saveTheme(effect.theme)
    }
}
