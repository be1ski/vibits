package space.be1ski.vibits.feature.memos.domain.repository

fun interface ConnectionTester {
  suspend operator fun invoke(
    baseUrl: String,
    token: String,
  ): Result<Unit>
}
