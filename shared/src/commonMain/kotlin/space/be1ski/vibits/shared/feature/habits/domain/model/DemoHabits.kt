package space.be1ski.vibits.shared.feature.habits.domain.model

import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

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
