package space.be1ski.vibits.feature.onboarding.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.habits.demoHabitNameKey
import space.be1ski.vibits.feature.habits.domain.model.DemoHabits
import space.be1ski.vibits.feature.onboarding.domain.model.CUSTOM_PRESET_ID
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

interface HabitPresetsDataSource {
  fun getPresets(): List<HabitPreset>
}

@Inject
@SingleIn(AppScope::class)
class HabitPresetsDataSourceImpl : HabitPresetsDataSource {
  override fun getPresets(): List<HabitPreset> =
    listOf(
      HabitPreset(id = DemoHabits.EXERCISE, nameKey = demoHabitNameKey(DemoHabits.EXERCISE)),
      HabitPreset(id = DemoHabits.WATER, nameKey = demoHabitNameKey(DemoHabits.WATER)),
      HabitPreset(id = DemoHabits.READING, nameKey = demoHabitNameKey(DemoHabits.READING)),
      HabitPreset(id = DemoHabits.MEDITATION, nameKey = demoHabitNameKey(DemoHabits.MEDITATION)),
      HabitPreset(id = DemoHabits.WALKING, nameKey = demoHabitNameKey(DemoHabits.WALKING)),
      HabitPreset(id = DemoHabits.LEARNING, nameKey = demoHabitNameKey(DemoHabits.LEARNING)),
      HabitPreset(id = DemoHabits.NO_SUGAR, nameKey = demoHabitNameKey(DemoHabits.NO_SUGAR)),
      HabitPreset(id = DemoHabits.EARLY_SLEEP, nameKey = demoHabitNameKey(DemoHabits.EARLY_SLEEP)),
      HabitPreset(id = CUSTOM_PRESET_ID, nameKey = "label_habit_preset_custom"),
    )
}
