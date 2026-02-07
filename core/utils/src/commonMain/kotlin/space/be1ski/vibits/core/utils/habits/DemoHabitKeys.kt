package space.be1ski.vibits.core.utils.habits

const val DEMO_HABIT_KEY_PREFIX = "demo_habit_"

fun demoHabitNameKey(habitId: String): String = "$DEMO_HABIT_KEY_PREFIX$habitId"
