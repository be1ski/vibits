package space.be1ski.vibits.shared.feature.onboarding.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import space.be1ski.vibits.shared.core.ui.theme.AppColors
import space.be1ski.vibits.shared.core.ui.theme.resolve
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingStep

@Composable
fun OnboardingScreen(
  state: OnboardingState,
  onAction: (OnboardingAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  LaunchedEffect(Unit) {
    onAction(OnboardingAction.StartOnboarding)
  }

  Surface(
    modifier = modifier.fillMaxSize(),
    color = AppColors.background.resolve(),
  ) {
    when (state.currentStep) {
      OnboardingStep.Welcome -> {
        WelcomeScreen(
          onContinue = { onAction(OnboardingAction.Continue) },
          onSkip = { onAction(OnboardingAction.Skip) },
          modifier = modifier.fillMaxSize(),
        )
      }

      OnboardingStep.ChoosePreset -> {
        ChoosePresetScreen(
          presets = state.presets,
          selectedPresetId = state.selectedPresetId,
          onSelectPreset = { onAction(OnboardingAction.SelectPreset(it)) },
          onContinue = { onAction(OnboardingAction.Continue) },
          onBack = { onAction(OnboardingAction.Back) },
          modifier = modifier.fillMaxSize(),
        )
      }

      OnboardingStep.HabitSetup -> {
        HabitSetupScreen(
          selectedPresetId = state.selectedPresetId,
          presets = state.presets,
          habitName = state.habitName,
          isCreating = state.isCreatingHabit,
          error = state.creationError,
          onUpdateName = { onAction(OnboardingAction.UpdateHabitName(it)) },
          onCreate = { onAction(OnboardingAction.CreateHabit) },
          onBack = { onAction(OnboardingAction.Back) },
          modifier = modifier.fillMaxSize(),
        )
      }

      OnboardingStep.Success -> {
        SuccessScreen(
          onMarkFirstCheckIn = { onAction(OnboardingAction.MarkFirstCheckIn) },
          onGoToDashboard = { onAction(OnboardingAction.GoToDashboard) },
          modifier = modifier.fillMaxSize(),
        )
      }
    }
  }
}
