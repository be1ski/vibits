package space.be1ski.vibits.shared.feature.habits.presentation.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.demo_habit_early_sleep
import space.be1ski.vibits.shared.demo_habit_exercise
import space.be1ski.vibits.shared.demo_habit_learning
import space.be1ski.vibits.shared.demo_habit_meditation
import space.be1ski.vibits.shared.demo_habit_no_sugar
import space.be1ski.vibits.shared.demo_habit_reading
import space.be1ski.vibits.shared.demo_habit_walking
import space.be1ski.vibits.shared.demo_habit_water
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig

/**
 * Returns localized label for demo habits when in demo mode.
 * Falls back to habit.label for non-demo habits or when not in demo mode.
 */
@Composable
fun HabitConfig.localizedLabel(demoMode: Boolean): String {
  if (!demoMode) return label
  return demoHabitLabel(tag) ?: label
}

@Composable
private fun demoHabitLabel(tag: String): String? =
  when (tag) {
    "#habits/exercise" -> stringResource(Res.string.demo_habit_exercise)
    "#habits/reading" -> stringResource(Res.string.demo_habit_reading)
    "#habits/meditation" -> stringResource(Res.string.demo_habit_meditation)
    "#habits/water" -> stringResource(Res.string.demo_habit_water)
    "#habits/learning" -> stringResource(Res.string.demo_habit_learning)
    "#habits/walking" -> stringResource(Res.string.demo_habit_walking)
    "#habits/no_sugar" -> stringResource(Res.string.demo_habit_no_sugar)
    "#habits/early_sleep" -> stringResource(Res.string.demo_habit_early_sleep)
    else -> null
  }
