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
internal fun rememberAllHabitsJustCompleted(todayHabits: TodayHabits): Boolean {
  val allDone = todayHabits.day?.let { it.totalHabits > 0 && it.count == it.totalHabits } ?: false

  var previouslyAllDone by remember { mutableStateOf<Boolean?>(null) }
  val justCompleted = shouldTriggerCelebration(previouslyAllDone, allDone)
  previouslyAllDone = allDone

  return justCompleted
}
