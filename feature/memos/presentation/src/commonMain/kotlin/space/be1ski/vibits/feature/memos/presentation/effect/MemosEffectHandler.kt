package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.flow.Flow
import space.be1ski.vibits.core.elm.EffectHandler
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction

class MemosEffectHandler(
  private val credentialsHandler: MemosCredentialsEffectHandler,
  private val loadHandler: MemosLoadEffectHandler,
  private val writeHandler: MemosWriteEffectHandler,
  private val syncHandler: MemosSyncEffectHandler,
) : EffectHandler<MemosEffect, MemosAction> {
  override fun invoke(effect: MemosEffect): Flow<MemosAction> =
    when (effect) {
      is MemosEffect.Credentials -> credentialsHandler(effect)
      is MemosEffect.Load -> loadHandler(effect)
      is MemosEffect.Write -> writeHandler(effect)
      is MemosEffect.Sync -> syncHandler(effect)
    }
}
