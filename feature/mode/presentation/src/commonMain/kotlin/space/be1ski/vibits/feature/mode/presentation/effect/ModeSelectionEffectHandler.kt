package space.be1ski.vibits.feature.mode.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction

class ModeSelectionEffectHandler(
  private val credentialsHandler: ModeSelectionCredentialsEffectHandler,
  private val modeHandler: ModeSelectionModeEffectHandler,
) : EffectHandler<ModeSelectionEffect.Command, ModeSelectionAction> {
  override fun invoke(command: ModeSelectionEffect.Command): Flow<ModeSelectionAction> =
    when (command) {
      is ModeSelectionEffect.Command.Credentials -> credentialsHandler(command)
      is ModeSelectionEffect.Command.Mode -> modeHandler(command)
    }
}
