package space.be1ski.memos.shared.feature.auth.domain.usecase

import space.be1ski.memos.shared.feature.auth.domain.model.Credentials
import space.be1ski.memos.shared.feature.auth.domain.repository.CredentialsRepository

class SaveCredentialsUseCase(
  private val credentialsRepository: CredentialsRepository
) {
  operator fun invoke(credentials: Credentials) {
    credentialsRepository.save(credentials)
  }
}
