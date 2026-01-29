package space.be1ski.vibits.shared.feature.mode.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionAction
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffect
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionState
import space.be1ski.vibits.shared.feature.mode.presentation.handler.ModeSelectionCredentialsEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.handler.ModeSelectionEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.handler.ModeSelectionModeEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.modeSelectionReducer

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
