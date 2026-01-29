package space.be1ski.vibits.shared.core.elm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

/**
 * Creates a flow that emits multiple actions.
 * Use when you need to emit multiple actions or have branching logic.
 *
 * @param A action type that must implement [Action] interface
 */
fun <A : Action> actions(block: suspend FlowCollector<A>.() -> Unit): Flow<A> = flow(block)

/**
 * Creates a flow that emits a single action.
 * Use for simple one-action emissions.
 *
 * @param A action type that must implement [Action] interface
 */
fun <A : Action> action(value: A): Flow<A> = flow { emit(value) }

/**
 * Creates a flow that executes a side effect but emits no actions.
 * Use when you need to perform work without emitting actions.
 *
 * @param A action type that must implement [Action] interface
 */
fun <A : Action> sideEffect(block: suspend () -> Unit): Flow<A> = flow { block() }

/**
 * Creates an empty flow that emits no actions.
 * Use only for true no-op commands (rare).
 *
 * @param A action type that must implement [Action] interface
 */
fun <A : Action> noActions(): Flow<A> = emptyFlow()
