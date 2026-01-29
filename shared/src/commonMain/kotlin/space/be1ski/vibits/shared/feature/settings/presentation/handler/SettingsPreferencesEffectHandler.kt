package space.be1ski.vibits.shared.feature.settings.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.sideEffect
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveThemeUseCase
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect

private const val TAG = "SettingsPreferencesEffect"

class SettingsPreferencesEffectHandler(
  private val saveLanguage: SaveLanguageUseCase,
  private val saveTheme: SaveThemeUseCase,
) : EffectHandler<SettingsEffect.Command.Preferences, SettingsAction> {
  override fun invoke(command: SettingsEffect.Command.Preferences): Flow<SettingsAction> =
    when (command) {
      is SettingsEffect.Command.SaveLanguage -> handleSaveLanguage(command)
      is SettingsEffect.Command.SaveTheme -> handleSaveTheme(command)
    }

  private fun handleSaveLanguage(command: SettingsEffect.Command.SaveLanguage): Flow<SettingsAction> =
    sideEffect {
      Log.i(TAG, "Language changed to ${command.language}")
      saveLanguage(command.language)
    }

  private fun handleSaveTheme(command: SettingsEffect.Command.SaveTheme): Flow<SettingsAction> =
    sideEffect {
      Log.i(TAG, "Theme changed to ${command.theme}")
      saveTheme(command.theme)
    }
}
