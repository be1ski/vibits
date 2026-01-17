@file:Suppress("RedundantVisibilityModifier")

package space.be1ski.vibits.shared.core.elm

/**
 * ReducerResult extensions to simplify result creation. But take a look on the 'ReducerDsl.kt'.
 */

public fun <State, Effect> State.only(): ReducerResult<State, Effect> = ReducerResult(this, emptyList())

public fun <State, Effect> State.andEffects(vararg effects: Effect): ReducerResult<State, Effect> = ReducerResult(this, effects.toList())

public infix fun <State, Effect> State.andEffect(effect: Effect): ReducerResult<State, Effect> = ReducerResult(this, listOf(effect))

public infix fun <State, Effect> State.andEffects(effects: List<Effect>): ReducerResult<State, Effect> = ReducerResult(this, effects)
