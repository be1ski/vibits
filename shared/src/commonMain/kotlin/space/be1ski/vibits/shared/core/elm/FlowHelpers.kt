package space.be1ski.vibits.shared.core.elm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

fun <A> actions(block: suspend FlowCollector<A>.() -> Unit): Flow<A> = flow(block)

fun <A> action(value: A): Flow<A> = flow { emit(value) }

fun <A> sideEffect(block: suspend () -> Unit): Flow<A> = flow { block() }

fun <A> noActions(): Flow<A> = emptyFlow()
