package space.be1ski.vibits.shared.feature.onboarding.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.feature.habits.domain.model.DemoHabits
import space.be1ski.vibits.shared.feature.onboarding.domain.model.HabitPreset

interface HabitPresetsDataSource {
  fun getPresets(): List<HabitPreset>
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class HabitPresetsDataSourceImpl : HabitPresetsDataSource {
  override fun getPresets(): List<HabitPreset> =
    listOf(
      HabitPreset(id = DemoHabits.EXERCISE, nameKey = "demo_habit_exercise"),
      HabitPreset(id = DemoHabits.WATER, nameKey = "demo_habit_water"),
      HabitPreset(id = DemoHabits.READING, nameKey = "demo_habit_reading"),
      HabitPreset(id = DemoHabits.MEDITATION, nameKey = "demo_habit_meditation"),
      HabitPreset(id = DemoHabits.WALKING, nameKey = "demo_habit_walking"),
      HabitPreset(id = DemoHabits.LEARNING, nameKey = "demo_habit_learning"),
      HabitPreset(id = DemoHabits.NO_SUGAR, nameKey = "demo_habit_no_sugar"),
      HabitPreset(id = DemoHabits.EARLY_SLEEP, nameKey = "demo_habit_early_sleep"),
      HabitPreset(id = "custom", nameKey = "label_habit_preset_custom"),
    )
}
