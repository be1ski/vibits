package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.core.utils.habits.DemoHabitIds
import space.be1ski.vibits.core.utils.habits.demoHabitNameKey
import space.be1ski.vibits.feature.memos.domain.model.PostTags

object DemoHabits {
  val TAGS = DemoHabitIds.ALL.map { "${PostTags.HABITS_PREFIX}$it" }.toSet()
}

fun HabitConfig.isDemoHabit(): Boolean = DemoHabits.TAGS.contains(tag)

fun HabitConfig.demoLabelKey(): String? {
  if (!isDemoHabit()) return null
  return demoHabitNameKey(tag.removePrefix(PostTags.HABITS_PREFIX))
}
