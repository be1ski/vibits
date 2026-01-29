package space.be1ski.vibits.shared.feature.habits.presentation.view.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.feature.habits.domain.model.DemoHabitStringKeys
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
 * Returns localized name for a demo habit by its string key.
 */
@Composable
fun localizedDemoHabitName(nameKey: String): String =
  when (nameKey) {
    DemoHabitStringKeys.EXERCISE -> stringResource(Res.string.demo_habit_exercise)
    DemoHabitStringKeys.WATER -> stringResource(Res.string.demo_habit_water)
    DemoHabitStringKeys.READING -> stringResource(Res.string.demo_habit_reading)
    DemoHabitStringKeys.MEDITATION -> stringResource(Res.string.demo_habit_meditation)
    DemoHabitStringKeys.WALKING -> stringResource(Res.string.demo_habit_walking)
    DemoHabitStringKeys.LEARNING -> stringResource(Res.string.demo_habit_learning)
    DemoHabitStringKeys.NO_SUGAR -> stringResource(Res.string.demo_habit_no_sugar)
    DemoHabitStringKeys.EARLY_SLEEP -> stringResource(Res.string.demo_habit_early_sleep)
    else -> nameKey
  }
