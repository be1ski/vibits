package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.core.ui.theme.ColorPalette
import kotlin.jvm.JvmInline

@JvmInline
value class HabitColor(
  val argb: Long,
)

val DefaultHabitColor = HabitColor(ColorPalette.first())
