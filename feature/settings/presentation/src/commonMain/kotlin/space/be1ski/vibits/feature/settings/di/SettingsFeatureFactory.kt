package space.be1ski.vibits.feature.settings.di

import space.be1ski.vibits.core.elm.Feature
import space.be1ski.vibits.core.elm.FeatureImpl
import space.be1ski.vibits.core.platform.app.AppDetails
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsCredentialsEffectHandler
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffectHandler
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsModeEffectHandler
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsPreferencesEffectHandler
import space.be1ski.vibits.feature.settings.presentation.reducer.settingsReducer
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

fun createSettingsFeature(
  dependencies: SettingsDependencies,
  initialMode: AppMode,
  appDetails: AppDetails,
  initialSyncDebounceSeconds: Int,
  initialState: SettingsState = SettingsState(),
): Feature<SettingsAction, SettingsState, SettingsEffect.Command, SettingsEffect.Notification> =
  FeatureImpl(
    initialState =
      initialState.copy(
        appMode = initialMode,
        appDetails = appDetails,
        selectedSyncDebounceSeconds = initialSyncDebounceSeconds,
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
            saveSyncDebounce = dependencies.saveSyncDebounce,
          ),
      ),
  )
