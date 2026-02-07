package space.be1ski.vibits.core.ui.habits

import androidx.compose.runtime.Composable
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

@Composable
fun localizedDemoHabitName(nameKey: String): String =
  when (nameKey.removePrefix(DEMO_HABIT_KEY_PREFIX)) {
    "exercise" -> stringResource(Res.string.demo_habit_exercise)
    "water" -> stringResource(Res.string.demo_habit_water)
    "reading" -> stringResource(Res.string.demo_habit_reading)
    "meditation" -> stringResource(Res.string.demo_habit_meditation)
    "walking" -> stringResource(Res.string.demo_habit_walking)
    "learning" -> stringResource(Res.string.demo_habit_learning)
    "no_sugar" -> stringResource(Res.string.demo_habit_no_sugar)
    "early_sleep" -> stringResource(Res.string.demo_habit_early_sleep)
    else -> nameKey
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
