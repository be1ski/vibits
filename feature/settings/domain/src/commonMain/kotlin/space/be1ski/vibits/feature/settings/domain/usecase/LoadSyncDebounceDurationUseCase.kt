package space.be1ski.vibits.feature.settings.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository
import kotlin.time.Duration

@Inject
class LoadSyncDebounceDurationUseCase(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(): Duration = preferencesRepository.load().memosAutoSyncDebounceDuration
}
