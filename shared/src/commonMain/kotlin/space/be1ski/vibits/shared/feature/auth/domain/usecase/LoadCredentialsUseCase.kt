package space.be1ski.vibits.shared.feature.auth.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository

@Inject
class LoadCredentialsUseCase(
  private val credentialsRepository: CredentialsRepository,
) {
  operator fun invoke(): Credentials = credentialsRepository.load()
}
