package space.be1ski.vibits.shared.feature.settings.di

import space.be1ski.vibits.shared.app.domain.model.AppDetails
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffectHandler
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsState
import space.be1ski.vibits.shared.feature.settings.presentation.settingsReducer

fun createSettingsFeature(
  dependencies: SettingsDependencies,
  initialMode: AppMode,
  appDetails: AppDetails,
  initialState: SettingsState = SettingsState(),
): Feature<SettingsAction, SettingsState, SettingsEffect> =
  FeatureImpl(
    initialState =
      initialState.copy(
        appMode = initialMode,
        appDetails = appDetails,
      ),
    reducer = settingsReducer,
    effectHandler =
      SettingsEffectHandler(
        validateCredentials = { baseUrl, token -> dependencies.validateCredentials(baseUrl, token) },
        switchAppMode = { mode -> dependencies.switchAppMode(mode) },
        saveCredentials = { credentials -> dependencies.saveCredentials(credentials) },
        resetApp = { dependencies.resetApp() },
        saveLanguage = { language -> dependencies.saveLanguage(language) },
        saveTheme = { theme -> dependencies.saveTheme(theme) },
      ),
  )
