package space.be1ski.vibits.shared.feature.mode.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.onboarding.data.OnboardingStore
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

@Inject
class ResetAppUseCase(
  private val appModeRepository: AppModeRepository,
  private val credentialsRepository: CredentialsRepository,
  private val preferencesRepository: PreferencesRepository,
  private val onboardingStore: OnboardingStore,
) {
  operator fun invoke() {
    credentialsRepository.save(Credentials(baseUrl = "", token = ""))
    preferencesRepository.save(UserPreferences(TimeRangeTab.WEEKS, TimeRangeTab.WEEKS))
    appModeRepository.saveMode(AppMode.NOT_SELECTED)
    onboardingStore.reset()
  }
}
