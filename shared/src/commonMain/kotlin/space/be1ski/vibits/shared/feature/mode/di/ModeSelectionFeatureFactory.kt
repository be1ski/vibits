package space.be1ski.vibits.shared.feature.mode.di

import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.elm.FeatureImpl
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionAction
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffect
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffectHandler
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionState
import space.be1ski.vibits.shared.feature.mode.presentation.modeSelectionReducer

fun createModeSelectionFeature(
  validateCredentials: ValidateCredentialsUseCase,
  saveCredentials: SaveCredentialsUseCase,
  saveAppMode: SaveAppModeUseCase,
  initialState: ModeSelectionState = ModeSelectionState(),
): Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect> =
  FeatureImpl(
    initialState = initialState,
    reducer = modeSelectionReducer,
    effectHandler =
      ModeSelectionEffectHandler(
        validateCredentials = validateCredentials,
        saveCredentials = saveCredentials,
        saveAppMode = saveAppMode,
      ),
  )
