package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.core.utils.habits.DemoHabitStringKeys
import space.be1ski.vibits.feature.memos.domain.model.PostTags

/**
 * Predefined demo habits configuration.
 */
object DemoHabits {
  const val EXERCISE = "exercise"
  const val READING = "reading"
  const val MEDITATION = "meditation"
  const val WATER = "water"
  const val LEARNING = "learning"
  const val WALKING = "walking"
  const val NO_SUGAR = "no_sugar"
  const val EARLY_SLEEP = "early_sleep"

  val TAGS =
    setOf(
      "${PostTags.HABITS_PREFIX}$EXERCISE",
      "${PostTags.HABITS_PREFIX}$READING",
      "${PostTags.HABITS_PREFIX}$MEDITATION",
      "${PostTags.HABITS_PREFIX}$WATER",
      "${PostTags.HABITS_PREFIX}$LEARNING",
      "${PostTags.HABITS_PREFIX}$WALKING",
      "${PostTags.HABITS_PREFIX}$NO_SUGAR",
      "${PostTags.HABITS_PREFIX}$EARLY_SLEEP",
    )
}

/**
 * Checks if the habit is a predefined demo habit.
 */
fun HabitConfig.isDemoHabit(): Boolean = DemoHabits.TAGS.contains(tag)

/**
 * Returns the string resource key for a demo habit's localized label, or null if not a demo habit.
 */
fun HabitConfig.demoLabelKey(): String? =
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
