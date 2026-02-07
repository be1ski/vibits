package space.be1ski.vibits.core.utils.habits

object DemoHabitIds {
  const val EXERCISE = "exercise"
  const val READING = "reading"
  const val MEDITATION = "meditation"
  const val WATER = "water"
  const val LEARNING = "learning"
  const val WALKING = "walking"
  const val NO_SUGAR = "no_sugar"
  const val EARLY_SLEEP = "early_sleep"

  val ALL = setOf(EXERCISE, READING, MEDITATION, WATER, LEARNING, WALKING, NO_SUGAR, EARLY_SLEEP)
}

const val DEMO_HABIT_KEY_PREFIX = "demo_habit_"

fun demoHabitNameKey(habitId: String): String = "$DEMO_HABIT_KEY_PREFIX$habitId"
