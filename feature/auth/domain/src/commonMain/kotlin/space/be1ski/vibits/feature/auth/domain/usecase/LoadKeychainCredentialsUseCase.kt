package space.be1ski.vibits.feature.auth.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository

@Inject
class LoadKeychainCredentialsUseCase(
  private val credentialsRepository: CredentialsRepository,
) {
  operator fun invoke(): Credentials? = credentialsRepository.loadFromSecureStorage()

  fun isAvailable(): Boolean = credentialsRepository.isSecureStorageAvailable()
}
