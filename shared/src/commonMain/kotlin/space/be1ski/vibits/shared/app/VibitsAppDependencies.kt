package space.be1ski.vibits.shared.app

import space.be1ski.vibits.shared.domain.usecase.LoadAppDetailsUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.shared.feature.memos.presentation.MemosUseCases
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsUseCases

/**
 * Groups all dependencies needed by VibitsApp.
 * This allows a single injection point instead of multiple koinInject() calls.
 */
internal data class VibitsAppDependencies(
  val loadPreferencesUseCase: LoadPreferencesUseCase,
  val saveTimeRangeTabUseCase: SaveTimeRangeTabUseCase,
  val loadAppDetailsUseCase: LoadAppDetailsUseCase,
  val loadAppModeUseCase: LoadAppModeUseCase,
  val calculateSuccessRateUseCase: CalculateSuccessRateUseCase,
  val memosRepository: MemosRepository,
  val memosUseCases: MemosUseCases,
  val settingsUseCases: SettingsUseCases,
)
