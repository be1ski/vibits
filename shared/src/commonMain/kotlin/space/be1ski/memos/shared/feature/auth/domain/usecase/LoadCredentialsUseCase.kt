package space.be1ski.memos.shared.feature.auth.domain.usecase

import space.be1ski.memos.shared.feature.auth.domain.model.Credentials
import space.be1ski.memos.shared.feature.auth.domain.repository.CredentialsRepository

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
