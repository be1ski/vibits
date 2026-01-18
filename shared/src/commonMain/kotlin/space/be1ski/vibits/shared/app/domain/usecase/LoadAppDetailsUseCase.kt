package space.be1ski.vibits.shared.app.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.app.data.AppDetailsProvider
import space.be1ski.vibits.shared.app.domain.model.AppDetails

/**
 * Loads app details for settings screen.
 */
@Inject
class LoadAppDetailsUseCase(
  private val appDetailsProvider: AppDetailsProvider,
) {
  operator fun invoke(): AppDetails = appDetailsProvider.load()
}
