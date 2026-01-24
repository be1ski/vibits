package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitConfigTest {
  @Test
  fun `when accessing HABIT_COLORS then returns predefined palette`() {
    assertTrue(HABIT_COLORS.isNotEmpty())
    assertEquals(10, HABIT_COLORS.size)
    assertEquals(0xFF4CAF50L, HABIT_COLORS.first()) // Green
  }

  @Test
  fun `when accessing DEFAULT_HABIT_COLOR then returns green`() {
    assertEquals(0xFF4CAF50L, DEFAULT_HABIT_COLOR)
  }

  @Test
  fun `when creating HabitConfig without color then uses default`() {
    val config = HabitConfig(tag = "#habits/test", label = "Test")
    assertEquals(DEFAULT_HABIT_COLOR, config.color)
  }

  @Test
  fun `when creating HabitConfig with color then uses provided color`() {
    val config = HabitConfig(tag = "#habits/test", label = "Test", color = 0xFF0000L)
    assertEquals(0xFF0000L, config.color)
  }
}
