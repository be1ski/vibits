package space.be1ski.memos.shared.feature.auth.domain.usecase

import space.be1ski.memos.shared.feature.auth.domain.model.Credentials
import space.be1ski.memos.shared.feature.auth.domain.repository.CredentialsRepository

/**
 * Persists credentials locally.
 */
class SaveCredentialsUseCase(
  private val credentialsRepository: CredentialsRepository
) {
  /**
   * Saves credentials to the repository.
   */
  operator fun invoke(credentials: Credentials) {
    credentialsRepository.save(credentials)
  }
}
