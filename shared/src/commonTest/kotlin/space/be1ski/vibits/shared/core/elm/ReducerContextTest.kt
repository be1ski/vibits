package space.be1ski.vibits.shared.core.elm

import kotlin.test.Test
import kotlin.test.assertEquals

class ReducerContextTest {
  private data class TestState(
    val value: Int = 0,
    val name: String = "",
  )

  private sealed interface TestEffect {
    data object EffectA : TestEffect

    data object EffectB : TestEffect

    data class EffectC(
      val data: String,
    ) : TestEffect
  }

  @Test
  fun `when no changes then getResult returns initial state with empty effects`() {
    val context = ReducerContext<TestState, TestEffect>()
    val initialState = TestState(value = 42)

    val result = context.getResult(initialState)

    assertEquals(initialState, result.state)
    assertEquals(emptyList(), result.effects)
  }

  @Test
  fun `when state called with lambda then transformation is applied`() {
    val context = ReducerContext<TestState, TestEffect>()
    context.state { copy(value = value + 10) }

    val result = context.getResult(TestState(value = 5))

    assertEquals(15, result.state.value)
  }

  @Test
  fun `when state called with direct value then state is replaced`() {
    val context = ReducerContext<TestState, TestEffect>()
    val newState = TestState(value = 100, name = "new")
    context.state(newState)

    val result = context.getResult(TestState(value = 1, name = "old"))

    assertEquals(newState, result.state)
  }

  @Test
  fun `when state called multiple times then last call wins`() {
    val context = ReducerContext<TestState, TestEffect>()
    context.state { copy(value = 10) }
    context.state { copy(value = 20) }
    context.state { copy(value = 30) }

    val result = context.getResult(TestState())

    assertEquals(30, result.state.value)
  }

  @Test
  fun `when effect called then effect is added`() {
    val context = ReducerContext<TestState, TestEffect>()
    context.effect(TestEffect.EffectA)

    val result = context.getResult(TestState())

    assertEquals(listOf(TestEffect.EffectA), result.effects)
  }

  @Test
  fun `when effect called multiple times then effects accumulate`() {
    val context = ReducerContext<TestState, TestEffect>()
    context.effect(TestEffect.EffectA)
    context.effect(TestEffect.EffectB)
    context.effect(TestEffect.EffectC("data"))

    val result = context.getResult(TestState())

    assertEquals(
      listOf(TestEffect.EffectA, TestEffect.EffectB, TestEffect.EffectC("data")),
      result.effects,
    )
  }

  @Test
  fun `when effects called with vararg then all effects are added`() {
    val context = ReducerContext<TestState, TestEffect>()
    context.effects(TestEffect.EffectA, TestEffect.EffectB)

    val result = context.getResult(TestState())

    assertEquals(listOf(TestEffect.EffectA, TestEffect.EffectB), result.effects)
  }

  @Test
  fun `when effects called with list then all effects are added`() {
    val context = ReducerContext<TestState, TestEffect>()
    val effectList = listOf(TestEffect.EffectA, TestEffect.EffectC("test"))
    context.effects(effectList)

    val result = context.getResult(TestState())

    assertEquals(effectList, result.effects)
  }

  @Test
  fun `when state and effects combined then both are captured`() {
    val context = ReducerContext<TestState, TestEffect>()
    context.state { copy(value = 42, name = "updated") }
    context.effect(TestEffect.EffectA)
    context.effects(TestEffect.EffectB, TestEffect.EffectC("combo"))

    val result = context.getResult(TestState())

    assertEquals(TestState(value = 42, name = "updated"), result.state)
    assertEquals(
      listOf(TestEffect.EffectA, TestEffect.EffectB, TestEffect.EffectC("combo")),
      result.effects,
    )
  }

  @Test
  fun `when only effect without state change then initial state is preserved`() {
    val context = ReducerContext<TestState, TestEffect>()
    context.effect(TestEffect.EffectA)

    val initialState = TestState(value = 99, name = "preserved")
    val result = context.getResult(initialState)

    assertEquals(initialState, result.state)
    assertEquals(listOf(TestEffect.EffectA), result.effects)
  }
}
