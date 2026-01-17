package space.be1ski.vibits.shared.feature.settings.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials

class SettingsEffectHandler(
  private val useCases: SettingsUseCases,
) : EffectHandler<SettingsEffect, SettingsAction> {
  override fun invoke(effect: SettingsEffect): Flow<SettingsAction> =
    when (effect) {
      is SettingsEffect.ValidateCredentials -> handleValidateCredentials(effect)
      is SettingsEffect.SwitchMode -> handleSwitchMode(effect)
      is SettingsEffect.SaveCredentials -> handleSaveCredentials(effect)
      is SettingsEffect.ResetApp -> handleResetApp()
      is SettingsEffect.SaveLanguage -> handleSaveLanguage(effect)
      is SettingsEffect.SaveTheme -> handleSaveTheme(effect)
      // Parent notification effects are not handled here - they flow through to VibitsApp
      is SettingsEffect.NotifyModeChanged,
      is SettingsEffect.NotifyResetCompleted,
      is SettingsEffect.NotifyCredentialsSaved,
      is SettingsEffect.NotifyLanguageChanged,
      is SettingsEffect.NotifyThemeChanged,
      is SettingsEffect.NotifyDialogClosed,
      -> emptyFlow()
    }

  private fun handleValidateCredentials(effect: SettingsEffect.ValidateCredentials): Flow<SettingsAction> =
    flow {
      useCases
        .validateCredentials(effect.baseUrl, effect.token)
        .onSuccess { emit(SettingsAction.ValidationSucceeded) }
        .onFailure { emit(SettingsAction.ValidationFailed("connection_failed")) }
    }

  private fun handleSwitchMode(effect: SettingsEffect.SwitchMode): Flow<SettingsAction> =
    flow {
      useCases.switchAppMode(effect.mode)
      emit(SettingsAction.ModeSwitched)
    }

  private fun handleSaveCredentials(effect: SettingsEffect.SaveCredentials): Flow<SettingsAction> =
    flow {
      useCases.saveCredentials(Credentials(effect.baseUrl, effect.token))
    }

  private fun handleResetApp(): Flow<SettingsAction> =
    flow {
      useCases.resetApp()
      emit(SettingsAction.ResetCompleted)
    }

  private fun handleSaveLanguage(effect: SettingsEffect.SaveLanguage): Flow<SettingsAction> =
    emptyFlow<SettingsAction>().also {
      useCases.saveLanguage(effect.language)
    }

  private fun handleSaveTheme(effect: SettingsEffect.SaveTheme): Flow<SettingsAction> =
    emptyFlow<SettingsAction>().also {
      useCases.saveTheme(effect.theme)
    }
}
