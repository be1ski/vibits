package space.be1ski.vibits.shared.core.elm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Creates a child [CoroutineScope] for testing [Feature] implementations.
 *
 * This scope inherits the test's [CoroutineContext] (including the test dispatcher)
 * but has its own [Job], allowing it to be cancelled independently without affecting
 * the parent test scope.
 *
 * Example usage with UnconfinedTestDispatcher:
 * ```
 * @Test
 * fun `when action sent then state is updated`() =
 *   runTest(UnconfinedTestDispatcher()) {
 *     val feature = FeatureImpl(...)
 *     val featureScope = featureTestScope()
 *     feature.launchIn(featureScope)
 *
 *     feature.send(MyAction.DoSomething)
 *
 *     assertEquals(expected, feature.state.value)
 *     featureScope.cancel()
 *   }
 * ```
 *
 * @return A new [CoroutineScope] that can be cancelled independently
 */
fun CoroutineScope.featureTestScope(): CoroutineScope = CoroutineScope(coroutineContext + Job())
