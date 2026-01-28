package space.be1ski.vibits.shared.feature.onboarding.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.shared.app.di.AppScope
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
      HabitPreset(
        id = "water",
        nameKey = "label_habit_preset_water",
        nameEn = "Drink water",
        icon = "💧",
      ),
      HabitPreset(
        id = "stretch",
        nameKey = "label_habit_preset_stretch",
        nameEn = "Stretch",
        icon = "🧘",
      ),
      HabitPreset(
        id = "read",
        nameKey = "label_habit_preset_read",
        nameEn = "Read for 10 minutes",
        icon = "📚",
      ),
      HabitPreset(
        id = "walk",
        nameKey = "label_habit_preset_walk",
        nameEn = "Take a walk",
        icon = "🚶",
      ),
      HabitPreset(
        id = "custom",
        nameKey = "label_habit_preset_custom",
        nameEn = "Custom",
        icon = "✨",
      ),
    )
}
