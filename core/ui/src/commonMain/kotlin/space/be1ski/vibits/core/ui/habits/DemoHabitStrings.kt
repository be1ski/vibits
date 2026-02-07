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
import space.be1ski.vibits.core.utils.habits.DemoHabit

private val demoHabitResources: Map<DemoHabit, StringResource> =
  mapOf(
    DemoHabit.EXERCISE to Res.string.demo_habit_exercise,
    DemoHabit.WATER to Res.string.demo_habit_water,
    DemoHabit.READING to Res.string.demo_habit_reading,
    DemoHabit.MEDITATION to Res.string.demo_habit_meditation,
    DemoHabit.WALKING to Res.string.demo_habit_walking,
    DemoHabit.LEARNING to Res.string.demo_habit_learning,
    DemoHabit.NO_SUGAR to Res.string.demo_habit_no_sugar,
    DemoHabit.EARLY_SLEEP to Res.string.demo_habit_early_sleep,
  )

@Composable
fun localizedDemoHabitName(habit: DemoHabit): String = demoHabitResources[habit]?.let { stringResource(it) } ?: habit.id

@Composable
fun localizedHabitLabel(
  label: String,
  demoHabit: DemoHabit?,
  demoMode: Boolean,
): String {
  if (!demoMode || demoHabit == null) return label
  return localizedDemoHabitName(demoHabit)
}
