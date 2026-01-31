package space.be1ski.vibits.feature.onboarding.presentation.view

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import space.be1ski.vibits.core.ui.theme.AppColors
import space.be1ski.vibits.core.ui.theme.resolve
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingStep

@Composable
fun OnboardingScreen(
  state: OnboardingState,
  onAction: (OnboardingAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  LaunchedEffect(Unit) {
    onAction(OnboardingAction.Navigation.StartOnboarding)
  }

  Surface(
    modifier =
      modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing),
    color = AppColors.background.resolve(),
  ) {
    when (state.currentStep) {
      OnboardingStep.Welcome -> {
        WelcomeScreen(
          onContinue = { onAction(OnboardingAction.Navigation.Continue) },
          onSkip = { onAction(OnboardingAction.Navigation.Skip) },
          modifier = modifier.fillMaxSize(),
        )
      }

      OnboardingStep.ChoosePreset -> {
        ChoosePresetScreen(
          presets = state.presets,
          selectedPresetId = state.selectedPresetId,
          onSelectPreset = { presetId, localizedName ->
            onAction(OnboardingAction.Preset.SelectPreset(presetId, localizedName))
          },
          onContinue = { onAction(OnboardingAction.Navigation.Continue) },
          onBack = { onAction(OnboardingAction.Navigation.Back) },
          modifier = modifier.fillMaxSize(),
        )
      }

      OnboardingStep.HabitSetup -> {
        HabitSetupScreen(
          selectedPresetId = state.selectedPresetId,
          presets = state.presets,
          habitName = state.habitName,
          selectedColor = state.selectedColor,
          isCreating = state.isCreatingHabit,
          error = state.creationError,
          onUpdateName = { onAction(OnboardingAction.Habit.UpdateHabitName(it)) },
          onColorChange = { onAction(OnboardingAction.Habit.UpdateHabitColor(it)) },
          onCreate = { onAction(OnboardingAction.Habit.CreateHabit) },
          onBack = { onAction(OnboardingAction.Navigation.Back) },
          modifier = modifier.fillMaxSize(),
        )
      }

      OnboardingStep.Success -> {
        SuccessScreen(
          onMarkFirstCheckIn = { onAction(OnboardingAction.Completion.MarkFirstCheckIn) },
          onGoToDashboard = { onAction(OnboardingAction.Completion.GoToDashboard) },
          modifier = modifier.fillMaxSize(),
        )
      }
    }
  }
}
