package space.be1ski.vibits.shared.feature.auth.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository

@Inject
class SaveCredentialsUseCase(
  private val credentialsRepository: CredentialsRepository,
) {
  operator fun invoke(credentials: Credentials) {
    credentialsRepository.save(credentials)
  }
}
