package space.be1ski.vibits.feature.habits.domain.model

import space.be1ski.vibits.core.utils.habits.DemoHabit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DemoHabitsTest {
  @Test
  fun `when tag is demo exercise then demoHabit returns EXERCISE`() {
    val habit = HabitConfig(tag = "#habits/exercise", label = "Exercise")
    assertEquals(DemoHabit.EXERCISE, habit.demoHabit())
  }

  @Test
  fun `when tag is demo water then demoHabit returns WATER`() {
    val habit = HabitConfig(tag = "#habits/water", label = "Water")
    assertEquals(DemoHabit.WATER, habit.demoHabit())
  }

  @Test
  fun `when tag is demo reading then demoHabit returns READING`() {
    val habit = HabitConfig(tag = "#habits/reading", label = "Reading")
    assertEquals(DemoHabit.READING, habit.demoHabit())
  }

  @Test
  fun `when tag is demo meditation then demoHabit returns MEDITATION`() {
    val habit = HabitConfig(tag = "#habits/meditation", label = "Meditation")
    assertEquals(DemoHabit.MEDITATION, habit.demoHabit())
  }

  @Test
  fun `when tag is demo walking then demoHabit returns WALKING`() {
    val habit = HabitConfig(tag = "#habits/walking", label = "Walking")
    assertEquals(DemoHabit.WALKING, habit.demoHabit())
  }

  @Test
  fun `when tag is demo learning then demoHabit returns LEARNING`() {
    val habit = HabitConfig(tag = "#habits/learning", label = "Learning")
    assertEquals(DemoHabit.LEARNING, habit.demoHabit())
  }

  @Test
  fun `when tag is demo no_sugar then demoHabit returns NO_SUGAR`() {
    val habit = HabitConfig(tag = "#habits/no_sugar", label = "No Sugar")
    assertEquals(DemoHabit.NO_SUGAR, habit.demoHabit())
  }

  @Test
  fun `when tag is demo early_sleep then demoHabit returns EARLY_SLEEP`() {
    val habit = HabitConfig(tag = "#habits/early_sleep", label = "Early Sleep")
    assertEquals(DemoHabit.EARLY_SLEEP, habit.demoHabit())
  }

  @Test
  fun `when tag is not a demo habit then demoHabit returns null`() {
    val habit = HabitConfig(tag = "#habits/custom", label = "Custom")
    assertNull(habit.demoHabit())
  }
}
