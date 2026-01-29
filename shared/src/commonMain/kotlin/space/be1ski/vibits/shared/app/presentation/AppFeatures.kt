package space.be1ski.vibits.shared.app.presentation

import space.be1ski.vibits.shared.app.domain.model.AppState
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsState
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsState

/**
 * Container for all application TEA features.
 * Created by AppFeaturesFactory, lives in AppScope.
 */
class AppFeatures internal constructor(
  val app: Feature<AppAction, AppState, AppEffect, Nothing>,
  val memos: Feature<MemosAction, MemosState, MemosEffect, Nothing>,
  val habits: Feature<HabitsAction, HabitsState, HabitsEffect, Nothing>,
  val settings: Feature<SettingsAction, SettingsState, SettingsEffect.Command, SettingsEffect.Notification>,
)
