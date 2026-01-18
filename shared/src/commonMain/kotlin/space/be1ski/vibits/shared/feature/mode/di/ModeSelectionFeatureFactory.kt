package space.be1ski.vibits.shared.feature.mode.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionAction
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffect
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionState
import space.be1ski.vibits.shared.feature.mode.presentation.modeSelectionReducer

fun createModeSelectionFeature(
  dependencies: ModeSelectionDependencies,
  initialState: ModeSelectionState = ModeSelectionState(),
): Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect> =
  FeatureImpl(
    initialState = initialState,
    reducer = modeSelectionReducer,
    effectHandler =
      ModeSelectionEffectHandler(
        validateCredentials = { baseUrl, token -> dependencies.validateCredentials(baseUrl, token) },
        saveCredentials = { credentials -> dependencies.saveCredentials(credentials) },
        saveAppMode = { mode -> dependencies.saveAppMode(mode) },
      ),
  )
