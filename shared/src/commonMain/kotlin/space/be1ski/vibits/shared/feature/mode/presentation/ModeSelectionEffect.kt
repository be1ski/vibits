package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

sealed interface ModeSelectionEffect {
  // Async operations (handled by EffectHandler)
  data class ValidateCredentials(
    val baseUrl: String,
    val token: String,
  ) : ModeSelectionEffect

  data class SaveCredentials(
    val baseUrl: String,
    val token: String,
  ) : ModeSelectionEffect

  data class SaveMode(
    val mode: AppMode,
  ) : ModeSelectionEffect

  // Parent notifications (observed by AppRoot)
  data class NotifyModeSelected(
    val mode: AppMode,
  ) : ModeSelectionEffect
}
