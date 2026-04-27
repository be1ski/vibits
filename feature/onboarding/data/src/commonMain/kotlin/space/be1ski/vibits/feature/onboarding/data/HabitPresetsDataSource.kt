package space.be1ski.vibits.feature.onboarding.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.habits.DemoHabit
import space.be1ski.vibits.feature.onboarding.domain.model.HabitPreset

interface HabitPresetsDataSource {
  fun getPresets(): List<HabitPreset>
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class HabitPresetsDataSourceImpl : HabitPresetsDataSource {
  override fun getPresets(): List<HabitPreset> = DemoHabit.entries.map { HabitPreset(demoHabit = it) } + HabitPreset(demoHabit = null)
}
