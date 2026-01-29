package space.be1ski.vibits.shared.feature.onboarding.domain.model

import space.be1ski.vibits.shared.feature.habits.domain.labelFromTag
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

data class HabitPreset(
  val id: String,
  val icon: String? = null,
) {
  val tag: String = "${PostTags.HABITS_PREFIX}$id"
  val nameEn: String = labelFromTag(tag).replaceFirstChar { it.uppercase() }
  val nameKey: String = if (id == "custom") "label_habit_preset_custom" else "demo_habit_$id"
}
