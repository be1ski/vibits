package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.model.auth.Credentials
import space.be1ski.memos.shared.domain.repository.CredentialsRepository

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
