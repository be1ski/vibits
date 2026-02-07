package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.core.utils.habits.DemoHabit
import space.be1ski.vibits.feature.memos.domain.model.PostTags

fun HabitConfig.demoHabit(): DemoHabit? = DemoHabit.fromId(tag.removePrefix(PostTags.HABITS_PREFIX))
