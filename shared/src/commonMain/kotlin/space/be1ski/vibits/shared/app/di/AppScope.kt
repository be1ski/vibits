package space.be1ski.vibits.shared.app.di
import space.be1ski.vibits.shared.app.presentation.action.AppAction
import space.be1ski.vibits.shared.app.presentation.effect.AppEffect
import space.be1ski.vibits.shared.app.presentation.reducer.appReducer

/**
 * Scope marker for application-level singletons.
 * Used with @SingleIn(AppScope::class) to scope bindings to the app graph lifecycle.
 */
object AppScope
