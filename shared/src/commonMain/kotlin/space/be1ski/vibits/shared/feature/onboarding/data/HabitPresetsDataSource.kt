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
      HabitPreset(id = DemoHabits.EXERCISE, icon = "🏃"),
      HabitPreset(id = DemoHabits.WATER, icon = "💧"),
      HabitPreset(id = DemoHabits.READING, icon = "📚"),
      HabitPreset(id = DemoHabits.MEDITATION, icon = "🧘"),
      HabitPreset(id = DemoHabits.WALKING, icon = "🚶"),
      HabitPreset(id = DemoHabits.LEARNING, icon = "📖"),
      HabitPreset(id = DemoHabits.NO_SUGAR, icon = "🍬"),
      HabitPreset(id = DemoHabits.EARLY_SLEEP, icon = "😴"),
      HabitPreset(id = "custom", icon = "✨"),
    )
}
