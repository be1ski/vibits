package space.be1ski.vibits.shared.feature.mode.domain.usecase

import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * TEMPORARY: Fixes invalid state where Online mode is selected but credentials are missing.
 * This can happen for users who switched to Online mode before validation was added.
 * Remove this use case after a few releases when all users have migrated.
 */
class FixInvalidOnlineModeUseCase(
  private val loadAppModeUseCase: LoadAppModeUseCase,
  private val saveAppModeUseCase: SaveAppModeUseCase,
  private val loadCredentialsUseCase: LoadCredentialsUseCase,
) {
  operator fun invoke(): AppMode {
    val mode = loadAppModeUseCase()

    if (mode == AppMode.ONLINE) {
      val credentials = loadCredentialsUseCase()
      if (credentials.baseUrl.isBlank() || credentials.token.isBlank()) {
        saveAppModeUseCase(AppMode.NOT_SELECTED)
        return AppMode.NOT_SELECTED
      }
    }

    return mode
  }
}
