package space.be1ski.vibits.core.ui.habits

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.demo_habit_early_sleep
import space.be1ski.vibits.core.strings.generated.demo_habit_exercise
import space.be1ski.vibits.core.strings.generated.demo_habit_learning
import space.be1ski.vibits.core.strings.generated.demo_habit_meditation
import space.be1ski.vibits.core.strings.generated.demo_habit_no_sugar
import space.be1ski.vibits.core.strings.generated.demo_habit_reading
import space.be1ski.vibits.core.strings.generated.demo_habit_walking
import space.be1ski.vibits.core.strings.generated.demo_habit_water
import space.be1ski.vibits.core.utils.habits.DEMO_HABIT_KEY_PREFIX
import space.be1ski.vibits.core.utils.habits.DemoHabitIds

private val demoHabitResources: Map<String, StringResource> =
  mapOf(
    DemoHabitIds.EXERCISE to Res.string.demo_habit_exercise,
    DemoHabitIds.WATER to Res.string.demo_habit_water,
    DemoHabitIds.READING to Res.string.demo_habit_reading,
    DemoHabitIds.MEDITATION to Res.string.demo_habit_meditation,
    DemoHabitIds.WALKING to Res.string.demo_habit_walking,
    DemoHabitIds.LEARNING to Res.string.demo_habit_learning,
    DemoHabitIds.NO_SUGAR to Res.string.demo_habit_no_sugar,
    DemoHabitIds.EARLY_SLEEP to Res.string.demo_habit_early_sleep,
  )

@Composable
fun localizedDemoHabitName(nameKey: String): String {
  val habitId = nameKey.removePrefix(DEMO_HABIT_KEY_PREFIX)
  return demoHabitResources[habitId]?.let { stringResource(it) } ?: nameKey
}

@Composable
fun localizedHabitLabel(
  label: String,
  demoLabelKey: String?,
  demoMode: Boolean,
): String {
  if (!demoMode || demoLabelKey == null) return label
  return localizedDemoHabitName(demoLabelKey)
}
