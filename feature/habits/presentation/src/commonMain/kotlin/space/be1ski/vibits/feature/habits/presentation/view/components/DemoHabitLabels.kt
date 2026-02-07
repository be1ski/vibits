package space.be1ski.vibits.feature.habits.presentation.view.components
import androidx.compose.runtime.Composable
import space.be1ski.vibits.core.ui.habits.localizedDemoHabitName
import space.be1ski.vibits.feature.habits.domain.model.DemoHabitStringKeys
import space.be1ski.vibits.feature.habits.domain.model.DemoHabits
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.memos.domain.model.PostTags

/**
 * Returns localized label for demo habits when in demo mode.
 * Falls back to habit.label for non-demo habits or when not in demo mode.
 */
@Composable
fun HabitConfig.localizedLabel(demoMode: Boolean): String {
  if (!demoMode) return label
  return demoHabitLabel(tag)?.let { localizedDemoHabitName(it) } ?: label
}

private fun demoHabitLabel(tag: String): String? =
  when (tag) {
    "${PostTags.HABITS_PREFIX}${DemoHabits.EXERCISE}" -> DemoHabitStringKeys.EXERCISE
    "${PostTags.HABITS_PREFIX}${DemoHabits.READING}" -> DemoHabitStringKeys.READING
    "${PostTags.HABITS_PREFIX}${DemoHabits.MEDITATION}" -> DemoHabitStringKeys.MEDITATION
    "${PostTags.HABITS_PREFIX}${DemoHabits.WATER}" -> DemoHabitStringKeys.WATER
    "${PostTags.HABITS_PREFIX}${DemoHabits.LEARNING}" -> DemoHabitStringKeys.LEARNING
    "${PostTags.HABITS_PREFIX}${DemoHabits.WALKING}" -> DemoHabitStringKeys.WALKING
    "${PostTags.HABITS_PREFIX}${DemoHabits.NO_SUGAR}" -> DemoHabitStringKeys.NO_SUGAR
    "${PostTags.HABITS_PREFIX}${DemoHabits.EARLY_SLEEP}" -> DemoHabitStringKeys.EARLY_SLEEP
    else -> null
  }
