package space.be1ski.vibits.feature.onboarding.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.habits.DemoHabitIds
import space.be1ski.vibits.core.utils.habits.demoHabitNameKey
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
      HabitPreset(id = DemoHabitIds.EXERCISE, nameKey = demoHabitNameKey(DemoHabitIds.EXERCISE)),
      HabitPreset(id = DemoHabitIds.WATER, nameKey = demoHabitNameKey(DemoHabitIds.WATER)),
      HabitPreset(id = DemoHabitIds.READING, nameKey = demoHabitNameKey(DemoHabitIds.READING)),
      HabitPreset(id = DemoHabitIds.MEDITATION, nameKey = demoHabitNameKey(DemoHabitIds.MEDITATION)),
      HabitPreset(id = DemoHabitIds.WALKING, nameKey = demoHabitNameKey(DemoHabitIds.WALKING)),
      HabitPreset(id = DemoHabitIds.LEARNING, nameKey = demoHabitNameKey(DemoHabitIds.LEARNING)),
      HabitPreset(id = DemoHabitIds.NO_SUGAR, nameKey = demoHabitNameKey(DemoHabitIds.NO_SUGAR)),
      HabitPreset(id = DemoHabitIds.EARLY_SLEEP, nameKey = demoHabitNameKey(DemoHabitIds.EARLY_SLEEP)),
      HabitPreset(id = CUSTOM_PRESET_ID, nameKey = "label_habit_preset_custom"),
    )
}
