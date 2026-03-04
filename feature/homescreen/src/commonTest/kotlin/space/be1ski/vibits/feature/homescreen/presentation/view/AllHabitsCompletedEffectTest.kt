package space.be1ski.vibits.feature.homescreen.presentation.view

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AllHabitsCompletedEffectTest {
  @Test
  fun `when initial null to true then no celebration`() {
    val result = shouldTriggerCelebration(previouslyAllDone = null, currentlyAllDone = true)
    assertFalse(result)
  }

  @Test
  fun `when false to true then celebration`() {
    val result = shouldTriggerCelebration(previouslyAllDone = false, currentlyAllDone = true)
    assertTrue(result)
  }

  @Test
  fun `when true to true then no celebration`() {
    val result = shouldTriggerCelebration(previouslyAllDone = true, currentlyAllDone = true)
    assertFalse(result)
  }

  @Test
  fun `when true to false then no celebration`() {
    val result = shouldTriggerCelebration(previouslyAllDone = true, currentlyAllDone = false)
    assertFalse(result)
  }

  @Test
  fun `when false to false then no celebration`() {
    val result = shouldTriggerCelebration(previouslyAllDone = false, currentlyAllDone = false)
    assertFalse(result)
  }

  @Test
  fun `when null to false then no celebration`() {
    val result = shouldTriggerCelebration(previouslyAllDone = null, currentlyAllDone = false)
    assertFalse(result)
  }
}
