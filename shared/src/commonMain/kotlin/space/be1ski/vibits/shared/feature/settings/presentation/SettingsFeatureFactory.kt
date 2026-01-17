package space.be1ski.vibits.shared.feature.settings.presentation

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.domain.model.app.AppDetails
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

fun createSettingsFeature(
  useCases: SettingsUseCases,
  initialMode: AppMode,
  appDetails: AppDetails,
  initialState: SettingsState = SettingsState()
): Feature<SettingsAction, SettingsState, SettingsEffect> {
  return FeatureImpl(
    initialState = initialState.copy(
      appMode = initialMode,
      appDetails = appDetails
    ),
    reducer = settingsReducer,
    effectHandler = SettingsEffectHandler(useCases)
  )
}
