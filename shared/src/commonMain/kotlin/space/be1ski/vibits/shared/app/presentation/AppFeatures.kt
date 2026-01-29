package space.be1ski.vibits.shared.app.presentation
import space.be1ski.vibits.shared.app.domain.model.AppState
import space.be1ski.vibits.shared.app.presentation.action.AppAction
import space.be1ski.vibits.shared.app.presentation.effect.AppEffect
import space.be1ski.vibits.shared.app.presentation.reducer.appReducer
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.effect.HabitsEffect
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

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
