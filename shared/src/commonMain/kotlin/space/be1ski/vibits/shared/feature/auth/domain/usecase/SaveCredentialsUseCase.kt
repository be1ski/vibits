package space.be1ski.vibits.shared.feature.auth.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository

fun interface SaveCredentials {
  operator fun invoke(credentials: Credentials)
}

@Inject
class SaveCredentialsUseCase(
  private val credentialsRepository: CredentialsRepository,
) : SaveCredentials {
  override operator fun invoke(credentials: Credentials) {
    credentialsRepository.save(credentials)
  }
}
