package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

internal fun shouldTriggerCelebration(
  previouslyAllDone: Boolean?,
  currentlyAllDone: Boolean,
): Boolean = currentlyAllDone && previouslyAllDone == false

@Composable
internal fun rememberAllHabitsJustCompleted(
  todayHabits: TodayHabits,
  celebrateImmediately: Boolean = false,
): Boolean {
  val allDone = todayHabits.day?.let { it.totalHabits > 0 && it.count == it.totalHabits } ?: false

  val initialValue = if (celebrateImmediately) false else null
  var previouslyAllDone by remember { mutableStateOf<Boolean?>(initialValue) }
  val justCompleted = shouldTriggerCelebration(previouslyAllDone, allDone)
  previouslyAllDone = allDone

  return justCompleted
}
