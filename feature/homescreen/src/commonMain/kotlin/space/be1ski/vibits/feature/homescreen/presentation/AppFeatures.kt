package space.be1ski.vibits.feature.homescreen.presentation

import space.be1ski.vibits.core.elm.Feature
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.homescreen.presentation.effect.AppEffect
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

class AppFeatures internal constructor(
  val app: Feature<AppAction, AppState, AppEffect, Nothing>,
  val memos: Feature<MemosAction, MemosState, MemosEffect, Nothing>,
  val habits: Feature<HabitsAction, HabitsState, HabitsEffect, Nothing>,
  val settings: Feature<SettingsAction, SettingsState, SettingsEffect.Command, SettingsEffect.Notification>,
)
