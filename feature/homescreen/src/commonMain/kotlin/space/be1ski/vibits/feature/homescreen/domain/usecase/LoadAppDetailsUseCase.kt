package space.be1ski.vibits.feature.homescreen.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.core.platform.app.AppDetails
import space.be1ski.vibits.core.platform.app.AppDetailsProvider

/**
 * Loads app details for settings screen.
 */
@Inject
class LoadAppDetailsUseCase(
  private val appDetailsProvider: AppDetailsProvider,
) {
  operator fun invoke(): AppDetails = appDetailsProvider.load()
}
