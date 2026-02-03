package space.be1ski.vibits.feature.settings.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction

class SettingsEffectHandler(
  private val credentialsHandler: SettingsCredentialsEffectHandler,
  private val modeHandler: SettingsModeEffectHandler,
  private val preferencesHandler: SettingsPreferencesEffectHandler,
) : EffectHandler<SettingsEffect.Command, SettingsAction> {
  override fun invoke(command: SettingsEffect.Command): Flow<SettingsAction> =
    when (command) {
      is SettingsEffect.Command.Credentials -> credentialsHandler(command)
      is SettingsEffect.Command.Mode -> modeHandler(command)
      is SettingsEffect.Command.Preferences -> preferencesHandler(command)
    }
}
