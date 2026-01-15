package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.data.local.AppDetailsProvider
import space.be1ski.memos.shared.domain.model.app.AppDetails

/**
 * Loads app details for settings screen.
 */
class LoadAppDetailsUseCase(
  private val appDetailsProvider: AppDetailsProvider
) {
  operator fun invoke(): AppDetails = appDetailsProvider.load()
}
