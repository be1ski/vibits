package space.be1ski.vibits.shared.feature.habits.view.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.feature.habits.domain.model.DemoHabits
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.demo_habit_early_sleep
import space.be1ski.vibits.shared.generated.demo_habit_exercise
import space.be1ski.vibits.shared.generated.demo_habit_learning
import space.be1ski.vibits.shared.generated.demo_habit_meditation
import space.be1ski.vibits.shared.generated.demo_habit_no_sugar
import space.be1ski.vibits.shared.generated.demo_habit_reading
import space.be1ski.vibits.shared.generated.demo_habit_walking
import space.be1ski.vibits.shared.generated.demo_habit_water

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
    "${PostTags.HABITS_PREFIX}${DemoHabits.EXERCISE}" -> stringResource(Res.string.demo_habit_exercise)
    "${PostTags.HABITS_PREFIX}${DemoHabits.READING}" -> stringResource(Res.string.demo_habit_reading)
    "${PostTags.HABITS_PREFIX}${DemoHabits.MEDITATION}" -> stringResource(Res.string.demo_habit_meditation)
    "${PostTags.HABITS_PREFIX}${DemoHabits.WATER}" -> stringResource(Res.string.demo_habit_water)
    "${PostTags.HABITS_PREFIX}${DemoHabits.LEARNING}" -> stringResource(Res.string.demo_habit_learning)
    "${PostTags.HABITS_PREFIX}${DemoHabits.WALKING}" -> stringResource(Res.string.demo_habit_walking)
    "${PostTags.HABITS_PREFIX}${DemoHabits.NO_SUGAR}" -> stringResource(Res.string.demo_habit_no_sugar)
    "${PostTags.HABITS_PREFIX}${DemoHabits.EARLY_SLEEP}" -> stringResource(Res.string.demo_habit_early_sleep)
    else -> null
  }
