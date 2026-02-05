package space.be1ski.vibits.feature.homescreen.domain.usecase

import space.be1ski.vibits.feature.homescreen.domain.model.SuccessRateLevel

private const val GOOD_THRESHOLD = 0.8f
private const val MEDIUM_THRESHOLD = 0.5f

object GetSuccessRateLevelUseCase {
  operator fun invoke(rate: Float): SuccessRateLevel =
    when {
      rate >= GOOD_THRESHOLD -> SuccessRateLevel.GOOD
      rate >= MEDIUM_THRESHOLD -> SuccessRateLevel.MEDIUM
      else -> SuccessRateLevel.BAD
    }
}
