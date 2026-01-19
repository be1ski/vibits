package space.be1ski.vibits.shared.app.domain.usecase

import space.be1ski.vibits.shared.app.domain.model.SuccessRateLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSuccessRateLevelUseCaseTest {
  @Test
  fun `when rate is 1 then returns GOOD`() {
    val result = GetSuccessRateLevelUseCase(1.0f)

    assertEquals(SuccessRateLevel.GOOD, result)
  }

  @Test
  fun `when rate is 0_8 then returns GOOD`() {
    val result = GetSuccessRateLevelUseCase(0.8f)

    assertEquals(SuccessRateLevel.GOOD, result)
  }

  @Test
  fun `when rate is 0_79 then returns MEDIUM`() {
    val result = GetSuccessRateLevelUseCase(0.79f)

    assertEquals(SuccessRateLevel.MEDIUM, result)
  }

  @Test
  fun `when rate is 0_5 then returns MEDIUM`() {
    val result = GetSuccessRateLevelUseCase(0.5f)

    assertEquals(SuccessRateLevel.MEDIUM, result)
  }

  @Test
  fun `when rate is 0_49 then returns BAD`() {
    val result = GetSuccessRateLevelUseCase(0.49f)

    assertEquals(SuccessRateLevel.BAD, result)
  }

  @Test
  fun `when rate is 0 then returns BAD`() {
    val result = GetSuccessRateLevelUseCase(0.0f)

    assertEquals(SuccessRateLevel.BAD, result)
  }
}
