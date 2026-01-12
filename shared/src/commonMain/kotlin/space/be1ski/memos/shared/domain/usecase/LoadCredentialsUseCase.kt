package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.model.Credentials
import space.be1ski.memos.shared.domain.repository.CredentialsRepository

/**
 * Loads locally stored credentials.
 */
class LoadCredentialsUseCase(
  private val credentialsRepository: CredentialsRepository
) {
  /**
   * Returns stored credentials.
   */
  operator fun invoke(): Credentials = credentialsRepository.load()
}
