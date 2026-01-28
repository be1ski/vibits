package space.be1ski.vibits.shared.feature.onboarding.domain.model

data class HabitPreset(
  val id: String,
  val nameKey: String,
  val nameEn: String,
  val defaultSchedule: String = "daily",
  val defaultGoal: Int = 1,
  val icon: String? = null,
)
