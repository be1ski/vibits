package space.be1ski.vibits.feature.homescreen.presentation.view

import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction

internal class FeatureDispatchers(
  val dispatchApp: (AppAction) -> Unit,
  val dispatchMemos: (MemosAction) -> Unit,
  val dispatchHabits: (HabitsAction) -> Unit,
)
