package space.be1ski.vibits.feature.onboarding.domain.model

import space.be1ski.vibits.core.utils.habits.DemoHabit

const val CUSTOM_PRESET_ID = "custom"

data class HabitPreset(
  val demoHabit: DemoHabit?,
) {
  val id: String get() = demoHabit?.id ?: CUSTOM_PRESET_ID
}
