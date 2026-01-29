package space.be1ski.vibits.shared.feature.settings.di

import space.be1ski.vibits.shared.app.domain.model.AppDetails
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsCredentialsEffectHandler
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffectHandler
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsModeEffectHandler
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsPreferencesEffectHandler
import space.be1ski.vibits.shared.feature.settings.presentation.reducer.settingsReducer
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

fun createSettingsFeature(
  dependencies: SettingsDependencies,
  initialMode: AppMode,
  appDetails: AppDetails,
  initialState: SettingsState = SettingsState(),
): Feature<SettingsAction, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  FeatureImpl(
    initialState =
      initialState.copy(
        appMode = initialMode,
        appDetails = appDetails,
      ),
    reducer = settingsReducer,
    effectHandler =
      SettingsEffectHandler(
        credentialsHandler =
          SettingsCredentialsEffectHandler(
            connectionTester = dependencies.connectionTester,
            saveCredentials = dependencies.saveCredentials,
          ),
        modeHandler =
          SettingsModeEffectHandler(
            switchAppMode = dependencies.switchAppMode,
            resetApp = dependencies.resetApp,
            resetAppWithMemos = dependencies.resetAppWithMemos,
          ),
        preferencesHandler =
          SettingsPreferencesEffectHandler(
            saveLanguage = dependencies.saveLanguage,
            saveTheme = dependencies.saveTheme,
          ),
      ),
  )
