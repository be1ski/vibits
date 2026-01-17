package space.be1ski.vibits.shared.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.data.local.AppDetailsProvider
import space.be1ski.vibits.shared.domain.model.app.AppDetails

/**
 * Loads app details for settings screen.
 */
class LoadAppDetailsUseCase @Inject constructor(
  private val appDetailsProvider: AppDetailsProvider,
) {
  operator fun invoke(): AppDetails = appDetailsProvider.load()
}
