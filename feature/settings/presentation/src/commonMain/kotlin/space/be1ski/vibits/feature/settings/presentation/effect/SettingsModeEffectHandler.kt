package space.be1ski.vibits.feature.settings.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.core.elm.actions
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.feature.mode.domain.usecase.ResetAppUseCase
import space.be1ski.vibits.feature.mode.domain.usecase.ResetAppWithMemosUseCase
import space.be1ski.vibits.feature.mode.domain.usecase.SwitchAppModeUseCase
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction

private const val TAG = "SettingsModeEffect"

class SettingsModeEffectHandler(
  private val switchAppMode: SwitchAppModeUseCase,
  private val resetApp: ResetAppUseCase,
  private val resetAppWithMemos: ResetAppWithMemosUseCase,
) : EffectHandler<SettingsEffect.Command.Mode, SettingsAction> {
  override fun invoke(command: SettingsEffect.Command.Mode): Flow<SettingsAction> =
    when (command) {
      is SettingsEffect.Command.SwitchMode -> handleSwitchMode(command)
      SettingsEffect.Command.ResetApp -> handleResetApp()
      SettingsEffect.Command.ResetAppWithMemos -> handleResetAppWithMemos()
    }

  private fun handleSwitchMode(command: SettingsEffect.Command.SwitchMode): Flow<SettingsAction> =
    actions {
      Log.i(TAG, "Switching mode to ${command.mode}")
      switchAppMode(command.mode)
      emit(SettingsAction.Input.ModeSwitched)
    }

  private fun handleResetApp(): Flow<SettingsAction> =
    actions {
      Log.i(TAG, "Resetting app")
      resetApp()
      emit(SettingsAction.Reset.ResetCompleted)
    }

  private fun handleResetAppWithMemos(): Flow<SettingsAction> =
    actions {
      Log.i(TAG, "Resetting app with memos")
      resetAppWithMemos()
      emit(SettingsAction.Reset.ResetCompleted)
    }
}
