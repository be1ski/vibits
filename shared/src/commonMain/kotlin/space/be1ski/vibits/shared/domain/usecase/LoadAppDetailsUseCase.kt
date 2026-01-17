package space.be1ski.vibits.shared.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.data.local.AppDetailsProvider
import space.be1ski.vibits.shared.domain.model.app.AppDetails

/**
 * Loads app details for settings screen.
 */
@Inject
class LoadAppDetailsUseCase(
  private val appDetailsProvider: AppDetailsProvider,
) {
  operator fun invoke(): AppDetails = appDetailsProvider.load()
}
