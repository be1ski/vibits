package space.be1ski.vibits.feature.habits.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DemoHabitsTest {
  @Test
  fun `when tag is demo exercise then demoLabelKey returns exercise key`() {
    val habit = HabitConfig(tag = "#habits/exercise", label = "Exercise")
    assertEquals("demo_habit_exercise", habit.demoLabelKey())
  }

  @Test
  fun `when tag is demo water then demoLabelKey returns water key`() {
    val habit = HabitConfig(tag = "#habits/water", label = "Water")
    assertEquals("demo_habit_water", habit.demoLabelKey())
  }

  @Test
  fun `when tag is demo reading then demoLabelKey returns reading key`() {
    val habit = HabitConfig(tag = "#habits/reading", label = "Reading")
    assertEquals("demo_habit_reading", habit.demoLabelKey())
  }

  @Test
  fun `when tag is demo meditation then demoLabelKey returns meditation key`() {
    val habit = HabitConfig(tag = "#habits/meditation", label = "Meditation")
    assertEquals("demo_habit_meditation", habit.demoLabelKey())
  }

  @Test
  fun `when tag is demo walking then demoLabelKey returns walking key`() {
    val habit = HabitConfig(tag = "#habits/walking", label = "Walking")
    assertEquals("demo_habit_walking", habit.demoLabelKey())
  }

  @Test
  fun `when tag is demo learning then demoLabelKey returns learning key`() {
    val habit = HabitConfig(tag = "#habits/learning", label = "Learning")
    assertEquals("demo_habit_learning", habit.demoLabelKey())
  }

  @Test
  fun `when tag is demo no_sugar then demoLabelKey returns no_sugar key`() {
    val habit = HabitConfig(tag = "#habits/no_sugar", label = "No Sugar")
    assertEquals("demo_habit_no_sugar", habit.demoLabelKey())
  }

  @Test
  fun `when tag is demo early_sleep then demoLabelKey returns early_sleep key`() {
    val habit = HabitConfig(tag = "#habits/early_sleep", label = "Early Sleep")
    assertEquals("demo_habit_early_sleep", habit.demoLabelKey())
  }

  @Test
  fun `when tag is not a demo habit then demoLabelKey returns null`() {
    val habit = HabitConfig(tag = "#habits/custom", label = "Custom")
    assertNull(habit.demoLabelKey())
  }

  @Test
  fun `when tag is a demo habit then isDemoHabit returns true`() {
    val habit = HabitConfig(tag = "#habits/exercise", label = "Exercise")
    assertTrue(habit.isDemoHabit())
  }

  @Test
  fun `when tag is not a demo habit then isDemoHabit returns false`() {
    val habit = HabitConfig(tag = "#habits/custom", label = "Custom")
    assertEquals(false, habit.isDemoHabit())
  }
}
