package space.be1ski.vibits.shared.feature.auth.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository

class SaveCredentialsUseCase @Inject constructor(
  private val credentialsRepository: CredentialsRepository,
) {
  operator fun invoke(credentials: Credentials) {
    credentialsRepository.save(credentials)
  }
}
