package space.be1ski.vibits.shared.feature.memos.presentation.handler

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.shared.core.elm.EffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect

class MemosEffectHandler(
  private val credentialsHandler: MemosCredentialsEffectHandler,
  private val loadHandler: MemosLoadEffectHandler,
  private val writeHandler: MemosWriteEffectHandler,
) : EffectHandler<MemosEffect, MemosAction> {
  override fun invoke(effect: MemosEffect): Flow<MemosAction> =
    when (effect) {
      is MemosEffect.Credentials -> credentialsHandler(effect)
      is MemosEffect.Load -> loadHandler(effect)
      is MemosEffect.Write -> writeHandler(effect)
    }
}
