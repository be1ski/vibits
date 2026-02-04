package space.be1ski.vibits.feature.onboarding.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.feature.habits.domain.model.DemoHabitStringKeys
import space.be1ski.vibits.feature.habits.domain.model.DemoHabits
import space.be1ski.vibits.feature.onboarding.domain.model.CUSTOM_PRESET_ID
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

fun interface HabitPresetsDataSource {
  fun getPresets(): List<HabitPreset>
}

@Inject
@SingleIn(AppScope::class)
class HabitPresetsDataSourceImpl : HabitPresetsDataSource {
  override fun getPresets(): List<HabitPreset> =
    listOf(
      HabitPreset(id = DemoHabits.EXERCISE, nameKey = DemoHabitStringKeys.EXERCISE),
      HabitPreset(id = DemoHabits.WATER, nameKey = DemoHabitStringKeys.WATER),
      HabitPreset(id = DemoHabits.READING, nameKey = DemoHabitStringKeys.READING),
      HabitPreset(id = DemoHabits.MEDITATION, nameKey = DemoHabitStringKeys.MEDITATION),
      HabitPreset(id = DemoHabits.WALKING, nameKey = DemoHabitStringKeys.WALKING),
      HabitPreset(id = DemoHabits.LEARNING, nameKey = DemoHabitStringKeys.LEARNING),
      HabitPreset(id = DemoHabits.NO_SUGAR, nameKey = DemoHabitStringKeys.NO_SUGAR),
      HabitPreset(id = DemoHabits.EARLY_SLEEP, nameKey = DemoHabitStringKeys.EARLY_SLEEP),
      HabitPreset(id = CUSTOM_PRESET_ID, nameKey = "label_habit_preset_custom"),
    )
}
