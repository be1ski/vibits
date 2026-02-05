package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import space.be1ski.vibits.core.elm.Feature
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.homescreen.di.AppDependencies
import space.be1ski.vibits.feature.homescreen.di.AppGraph
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.mode.di.createModeSelectionFeature
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import space.be1ski.vibits.feature.onboarding.di.createOnboardingFeature
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState

internal data class FeaturesState(
  val modeSelection: Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification>,
  val onboarding: Feature<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification>,
  val app: AppFeatures,
)

@Composable
internal fun rememberFeaturesState(
  dependencies: AppDependencies,
  featuresVersion: Int,
  onModeSelected: (AppMode) -> Unit,
  onOnboardingCompleted: () -> Unit,
): FeaturesState {
  val modeSelectionFeature =
    key(featuresVersion) {
      rememberModeSelectionFeature(dependencies, onModeSelected)
    }
  val appFeatures = rememberAppFeatures(featuresVersion)
  val onboardingFeature =
    key(featuresVersion) {
      rememberOnboardingFeature(dependencies, appFeatures, onOnboardingCompleted)
    }

  return FeaturesState(modeSelectionFeature, onboardingFeature, appFeatures)
}

@Composable
private fun rememberAppFeatures(version: Int): AppFeatures =
  remember(version) {
    val factory = AppGraph.getFeaturesFactory()
    val scope = AppGraph.getAppScope()
    factory.create(scope)
  }

@Composable
private fun rememberModeSelectionFeature(
  dependencies: AppDependencies,
  onModeSelected: (AppMode) -> Unit,
): Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification> {
  val feature =
    remember {
      createModeSelectionFeature(dependencies = dependencies.modeSelectionDependencies)
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(feature) { feature.launchIn(scope) }
  LaunchedEffect(feature) {
    feature.notifications.collect { notification ->
      when (notification) {
        is ModeSelectionEffect.Notification.ModeSelected -> onModeSelected(notification.mode)
      }
    }
  }
  return feature
}

@Composable
private fun rememberOnboardingFeature(
  dependencies: AppDependencies,
  features: AppFeatures,
  onOnboardingCompleted: () -> Unit,
): Feature<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> {
  val feature =
    remember {
      createOnboardingFeature(dependencies = dependencies.onboardingDependencies)
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(feature) { feature.launchIn(scope) }
  LaunchedEffect(feature) {
    feature.notifications.collect { notification ->
      when (notification) {
        is OnboardingEffect.Notification.Completed,
        is OnboardingEffect.Notification.Skipped,
        -> onOnboardingCompleted()
        is OnboardingEffect.Notification.FirstCheckInCreated -> {
          features.memos.send(MemosAction.Loading.LoadMemos)
        }
      }
    }
  }
  return feature
}
