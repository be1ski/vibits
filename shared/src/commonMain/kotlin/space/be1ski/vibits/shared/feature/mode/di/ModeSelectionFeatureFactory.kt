package space.be1ski.vibits.shared.feature.mode.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.shared.feature.mode.presentation.effect.ModeSelectionCredentialsEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.effect.ModeSelectionEffect
import space.be1ski.vibits.shared.feature.mode.presentation.effect.ModeSelectionEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.effect.ModeSelectionModeEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.reducer.modeSelectionReducer
import space.be1ski.vibits.shared.feature.mode.presentation.state.ModeSelectionState

fun createModeSelectionFeature(
  dependencies: ModeSelectionDependencies,
  initialState: ModeSelectionState = ModeSelectionState(),
): Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification> =
  FeatureImpl(
    initialState = initialState,
    reducer = modeSelectionReducer,
    effectHandler =
      ModeSelectionEffectHandler(
        credentialsHandler =
          ModeSelectionCredentialsEffectHandler(
            connectionTester = dependencies.connectionTester,
            initializeCredentialsFromEnv = dependencies.initializeCredentialsFromEnv,
            loadCredentials = dependencies.loadCredentials,
            saveCredentials = dependencies.saveCredentials,
          ),
        modeHandler =
          ModeSelectionModeEffectHandler(
            saveAppMode = dependencies.saveAppMode,
          ),
      ),
    initialCommands = listOf(ModeSelectionEffect.Command.InitializeFromLocalConfig),
  )
