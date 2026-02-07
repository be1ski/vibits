package space.be1ski.vibits.feature.habits.presentation.view.components

import androidx.compose.runtime.Composable
import space.be1ski.vibits.core.ui.habits.localizedHabitLabel
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.demoHabit

@Composable
fun HabitConfig.localizedLabel(demoMode: Boolean): String = localizedHabitLabel(label, demoHabit(), demoMode)
