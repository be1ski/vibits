package space.be1ski.vibits.feature.settings.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.sideEffect
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.settings.domain.usecase.SaveLanguageUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveSyncDebounceUseCase
import space.be1ski.vibits.feature.settings.domain.usecase.SaveThemeUseCase
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction

private const val TAG = "SettingsPreferencesEffect"

class SettingsPreferencesEffectHandler(
  private val saveLanguage: SaveLanguageUseCase,
  private val saveTheme: SaveThemeUseCase,
  private val saveSyncDebounce: SaveSyncDebounceUseCase,
) : EffectHandler<SettingsEffect.Command.Preferences, SettingsAction> {
  override fun invoke(command: SettingsEffect.Command.Preferences): Flow<SettingsAction> =
    when (command) {
      is SettingsEffect.Command.SaveLanguage -> handleSaveLanguage(command)
      is SettingsEffect.Command.SaveTheme -> handleSaveTheme(command)
      is SettingsEffect.Command.SaveSyncDebounce -> handleSaveSyncDebounce(command)
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

  private fun handleSaveSyncDebounce(command: SettingsEffect.Command.SaveSyncDebounce): Flow<SettingsAction> =
    sideEffect {
      Log.i(TAG, "Sync debounce changed to ${command.seconds}s")
      saveSyncDebounce(command.seconds)
    }
}
