package space.be1ski.vibits.shared.feature.mode.presentation

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.core.elm.actions
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase

private const val TAG = "ModeModeEffect"

class ModeSelectionModeEffectHandler(
  private val saveAppMode: SaveAppModeUseCase,
) : EffectHandler<ModeSelectionEffect.Command.Mode, ModeSelectionAction> {
  override fun invoke(command: ModeSelectionEffect.Command.Mode): Flow<ModeSelectionAction> =
    when (command) {
      is ModeSelectionEffect.Command.SaveMode -> handleSaveMode(command)
    }

  private fun handleSaveMode(command: ModeSelectionEffect.Command.SaveMode): Flow<ModeSelectionAction> =
    actions {
      Log.i(TAG, "Saving mode: ${command.mode}")
      saveAppMode(command.mode)
    }
}
