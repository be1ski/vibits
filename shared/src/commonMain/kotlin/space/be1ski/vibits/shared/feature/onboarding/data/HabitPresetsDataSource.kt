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
        id = "exercise",
        nameKey = "demo_habit_exercise",
        nameEn = "Exercise",
        icon = "🏃",
      ),
      HabitPreset(
        id = "water",
        nameKey = "demo_habit_water",
        nameEn = "Drink Water",
        icon = "💧",
      ),
      HabitPreset(
        id = "reading",
        nameKey = "demo_habit_reading",
        nameEn = "Reading",
        icon = "📚",
      ),
      HabitPreset(
        id = "meditation",
        nameKey = "demo_habit_meditation",
        nameEn = "Meditation",
        icon = "🧘",
      ),
      HabitPreset(
        id = "walking",
        nameKey = "demo_habit_walking",
        nameEn = "10K Steps",
        icon = "🚶",
      ),
      HabitPreset(
        id = "learning",
        nameKey = "demo_habit_learning",
        nameEn = "Learning",
        icon = "📖",
      ),
      HabitPreset(
        id = "no_sugar",
        nameKey = "demo_habit_no_sugar",
        nameEn = "No Sugar",
        icon = "🍬",
      ),
      HabitPreset(
        id = "early_sleep",
        nameKey = "demo_habit_early_sleep",
        nameEn = "Sleep by 11pm",
        icon = "😴",
      ),
      HabitPreset(
        id = "custom",
        nameKey = "label_habit_preset_custom",
        nameEn = "Custom",
        icon = "✨",
      ),
    )
}
