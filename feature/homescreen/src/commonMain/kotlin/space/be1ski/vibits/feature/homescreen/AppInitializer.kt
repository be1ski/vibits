package space.be1ski.vibits.feature.homescreen

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope

/**
 * Application initialization orchestrator.
 * Handles startup tasks like cache warming, migrations, etc.
 */
@Inject
@SingleIn(AppScope::class)
class AppInitializer {
  operator fun invoke() {
    // Reserved for future startup tasks (cache warming, migrations, etc.)
  }
}
